package com.ericsson.mlcn.im.rules;

import com.ericsson.mlcraft.common.util.MOIDHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import no.axxessit.client.util.MgtUtil;
import no.axxessit.common.Logger;
import no.axxessit.common.val.AxxEnum;
import no.axxessit.mgt.Attribute;
import no.axxessit.mgt.AttributeList;
import no.axxessit.mgt.AxxMBTableMBean;
import no.axxessit.mgt.MBeanProxy;
import no.axxessit.mgt.MOID;
import no.axxessit.mgt.TableRef;
import no.axxessit.mgt.rule.AbstractDefaultRule;
import no.axxessit.mgt.rule.event.AttributeStateChangeEvent;
import no.axxessit.mgt.rule.inf.IContextProvider;

public class MLCNE1LoopsTableRule extends AbstractDefaultRule {

   private static final Logger log = Logger.getLogger();
   private final String ATTR_LINE_LOOP = "LineLoop";
   private final String ATTR_LOCAL_LOOP = "LocalLoop";
   private final String ATTR_INTERFACE = "Interface";
   private final String[] DISABLED_ATTRIBUTES = new String[]{"Interface", "LineLoop", "LocalLoop"};


   public AttributeList evaluate(Object sourceContext, AttributeList list, IContextProvider callback, Object userData) {
      if(callback != null && callback.getContexts() != null && callback.getContexts().length >= 1) {
         Attribute idAttr = list.getAttribute("Id");
         if(idAttr != null && idAttr.getValue() instanceof MOID) {
            MOID rowId = (MOID)idAttr.getValue();
            MOID neMoid = MgtUtil.getInstance().getNe(rowId);
            if(neMoid != null && neMoid.getClassname().equals("com.ericsson.mlcn.im.MLCN") && this.isNetSyncSupportedByNode(neMoid)) {
               List netSyncIndices = this.getSyncInterfaceIndices(neMoid);
               if(netSyncIndices.size() > 0) {
                  Object[] objs = callback.getContexts();
                  Object[] arr$ = objs;
                  int len$ = objs.length;

                  for(int i$ = 0; i$ < len$; ++i$) {
                     Object obj = arr$[i$];
                     if(obj != null && obj instanceof Map) {
                        Map row = (Map)obj;
                        if(row.containsKey("Id") && row.containsKey("LineLoop") && row.containsKey("LocalLoop") && row.containsKey("Interface")) {
                           MOID moid = (MOID)row.get("Id");
                           String index = MOIDHelper.Instance().getIdxString(moid);
                           if(netSyncIndices.contains(index)) {
                              this.disableRow(obj, this.DISABLED_ATTRIBUTES, userData);
                           }
                        }
                     }
                  }
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private List getSyncInterfaceIndices(MOID neMoid) {
      ArrayList indices = new ArrayList();

      try {
         Object e = MgtUtil.getInstance().getAttribute(neMoid, "NetSyncTable");
         if(e != null && e instanceof TableRef) {
            TableRef tableRef = (TableRef)e;
            AxxMBTableMBean table = (AxxMBTableMBean)MBeanProxy.createInstance(tableRef.getMoid(), MgtUtil.getInstance().getMgtService());
            MOID[] rows = table.getRows();
            if(rows != null && rows.length > 0) {
               MOID[] arr$ = rows;
               int len$ = rows.length;

               for(int i$ = 0; i$ < len$; ++i$) {
                  MOID row = arr$[i$];
                  indices.add(MOIDHelper.Instance().getIdxString(row));
               }
            }
         }
      } catch (Exception var11) {
         var11.printStackTrace();
      }

      return indices;
   }

   private boolean isNetSyncSupportedByNode(MOID neMoid) {
      try {
         AxxEnum e = (AxxEnum)MgtUtil.getInstance().getAttribute(neMoid, "CNSyncBoardCapability");
         if(e != null) {
            int cap = e.value();
            return cap == 1 || cap == 2 || cap == 3;
         }
      } catch (Exception var4) {
         log.warn(var4);
      }

      return false;
   }

   private void disableRow(Object row, String[] attrNames, Object userData) {
      String[] arr$ = attrNames;
      int len$ = attrNames.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         String attrName = arr$[i$];
         AttributeStateChangeEvent event = new AttributeStateChangeEvent(row, attrName, (Object)null, Boolean.FALSE);
         event.setUserData(userData);
         this.firePropertyChange(event);
      }

   }

   public String[] getSourceAttributeNames() {
      return new String[]{"LineLoop"};
   }

   public String getDestinationAttributeName() {
      return "Interface";
   }

}
