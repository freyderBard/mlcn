package com.ericsson.mlcn.im.rules;

import com.ericsson.client.appl.MLCraftFormViewer;
import com.ericsson.mlcraft.common.license.FormSaveCallback;
import com.ericsson.mlcraft.common.util.MOIDHelper;
import com.ericsson.mlcraft.common.util.RelationSearchUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import no.axxessit.client.gui.ApplicationContext;
import no.axxessit.client.gui.ApplicationManager;
import no.axxessit.client.gui.forms.AxxFormModel;
import no.axxessit.client.gui.forms.AxxFormNode;
import no.axxessit.client.gui.table.AxxMBTableModel;
import no.axxessit.client.util.MgtUtil;
import no.axxessit.common.Env;
import no.axxessit.common.Logger;
import no.axxessit.common.val.AxxEnum;
import no.axxessit.common.val.AxxStepInteger;
import no.axxessit.mgt.Attribute;
import no.axxessit.mgt.AxxMBTableMBean;
import no.axxessit.mgt.MBeanException;
import no.axxessit.mgt.MBeanProxy;
import no.axxessit.mgt.MOID;
import no.axxessit.mgt.MOIDList;
import no.axxessit.mgt.MoAttributeList;
import no.axxessit.mgt.OperationException;
import no.axxessit.mgt.TableRef;

public class NetSyncCallback extends FormSaveCallback {

   private static final Logger log = Logger.getLogger();
   private static final String ATTR_NET_SYNC_TABLE = "NetSyncTable";
   private static final String ATTR_SYNC_MODE = "SyncMode";
   private static final String ATTR_ID = "Id";
   private static final String ATTR_LABEL = "Label";
   private static final String ATTR_QUALITY_LEVEL = "SyncNomAssQualityLevel";
   private static final String ATTR_IS_NOT_ASSIGNED_DEFAULT_VALUE = "IsNotAssignedTheDefaultValue";
   private static final String ATTR_E1_LINELOOP = "LineLoop";
   private static final String ATTR_E1_LOCALLOOP = "LocalLoop";
   private static final Integer SYNC_INPUT_2MHz = Integer.valueOf(2);
   private static final Integer SYNC_INPUT_2Mbps = Integer.valueOf(3);
   private static final long maxX1s = 16L;
   private static final String MSG_BOX_TITLE = "Configure Network Sync";
   Map E1IndexToLabels;


   public boolean validate(ApplicationContext appContext, MoAttributeList[] list) {
      if(list != null && list.length == 1) {
         MoAttributeList attrs = list[0];
         Attribute netSyncTableAtt = attrs.getAttribute("NetSyncTable");
         if(netSyncTableAtt != null) {
            AxxMBTableModel netSyncTable = (AxxMBTableModel)netSyncTableAtt.getValue();
            Map deletedRows = netSyncTable.getDeletedRows();
            int deletedRowsCount = deletedRows.size();
            Map var28;
            AxxEnum var29;
            if(deletedRowsCount > 0) {
               if(deletedRowsCount == 1) {
                  Object[] var25 = deletedRows.keySet().toArray();
                  Object var27 = var25[0];
                  if(var27 instanceof Map) {
                     var28 = (Map)var27;
                     var29 = (AxxEnum)var28.get("SyncMode");
                     if(this.isInput(var29)) {
                        return true;
                     }

                     Map[] var31 = netSyncTable.getRows();
                     if(var31.length == 1) {
                        return true;
                     }

                     MOID var33;
                     String var37;
                     Map[] var39;
                     Map var40;
                     String var41;
                     if(var31.length == 2) {
                        var33 = (MOID)var28.get("Id");
                        var37 = MOIDHelper.Instance().getIdxString(var33);
                        var39 = netSyncTable.getRows();
                        var40 = this.sortRows(var39);
                        if(var37.equals(var40.get(NetSyncCallback.E1.E1_15TH))) {
                           return true;
                        }

                        var41 = (String)this.E1IndexToLabels.get(var40.get(NetSyncCallback.E1.E1_15TH)) + " needs to be deleted before " + (String)this.E1IndexToLabels.get(var40.get(NetSyncCallback.E1.E1_16TH)) + " can be deleted.";
                        appContext.showErrorDialog("Configure Network Sync", var41);
                        this.refreshForm();
                        return false;
                     }

                     if(var31.length == 3) {
                        var33 = (MOID)var28.get("Id");
                        var37 = MOIDHelper.Instance().getIdxString(var33);
                        var39 = netSyncTable.getRows();
                        var40 = this.sortRows(var39);
                        if(var37.equals(var40.get(NetSyncCallback.E1.E1_14TH))) {
                           return true;
                        }

                        if(var37.equals(var40.get(NetSyncCallback.E1.E1_15TH))) {
                           var41 = (String)this.E1IndexToLabels.get(var40.get(NetSyncCallback.E1.E1_14TH)) + " needs to be deleted before " + (String)this.E1IndexToLabels.get(var40.get(NetSyncCallback.E1.E1_15TH)) + " can be deleted.";
                           appContext.showErrorDialog("Configure Network Sync", var41);
                           this.refreshForm();
                           return false;
                        }

                        var41 = (String)this.E1IndexToLabels.get(var40.get(NetSyncCallback.E1.E1_15TH)) + " needs to be deleted before " + (String)this.E1IndexToLabels.get(var40.get(NetSyncCallback.E1.E1_16TH)) + " can be deleted.";
                        appContext.showErrorDialog("Configure Network Sync", var41);
                        this.refreshForm();
                        return false;
                     }

                     return false;
                  }

                  return false;
               }

               appContext.showErrorDialog("Configure Network Sync", "Only one row can be deleted at a time.");
               this.refreshForm();
               return false;
            }

            Map addedRows = netSyncTable.getAddedRows();
            if(addedRows.size() > 0) {
               MOID rows = (MOID)Env.getInstance().getObject("EQUIPMENT.MOID");
               Map[] firstRow = netSyncTable.getRows();
               Map firstRowSyncMode = this.getLastThreeE1FromE1LoopsTable(rows);
               List errorMessage = this.getEnabledE1LoopsIndices(firstRowSyncMode);
               MOIDList title = RelationSearchUtil.getChildren(rows, "com.ericsson.mlne.im.app.rl.common.logical.hop.Radio_Link_Hop", 2);
               int qualityLevel = 0;

               for(int index = title.size(); qualityLevel < index; ++qualityLevel) {
                  MOID msg = (MOID)title.get(qualityLevel);
                  MOID radioLinkConfMoid = new MOID("com.ericsson.mlcraft.im.app.rl.common.config.configRadioLink.ConfigRadioLink", msg.getObjectId().toString(), msg.getResourceId());

                  try {
                     AxxStepInteger e = (AxxStepInteger)MgtUtil.getInstance().getMgtService().getAttribute(radioLinkConfMoid, "NearEndConfig.NumberOfE1");
                     long diff = 16L - (long)e.value();
                     int existingRows = firstRow.length - addedRows.size();
                     if(diff <= (long)existingRows) {
                        String msg1 = "There isn\'t any unused E1 left!";
                        appContext.showErrorDialog("Configure Network Sync", msg1);
                        this.refreshForm();
                        return false;
                     }
                  } catch (OperationException var23) {
                     log.error(var23);
                  } catch (MBeanException var24) {
                     log.error(var24);
                  }
               }

               if(errorMessage.size() > 0) {
                  MOID var34;
                  String var35;
                  String var38;
                  if(firstRow.length == 1) {
                     var34 = (MOID)firstRowSyncMode.get(NetSyncCallback.E1.E1_16TH);
                     var35 = MOIDHelper.Instance().getIdxString(var34);
                     if(errorMessage.contains(var35)) {
                        var38 = "E1 loops are activated on the " + this.getInterfaceOfE1FromE1LoopsTable(var34) + "! Disable the loops on this E1 in order to continue.";
                        appContext.showErrorDialog("Configure Network Sync", var38);
                        this.refreshForm();
                        return false;
                     }
                  } else if(firstRow.length == 2) {
                     var34 = (MOID)firstRowSyncMode.get(NetSyncCallback.E1.E1_15TH);
                     var35 = MOIDHelper.Instance().getIdxString(var34);
                     if(errorMessage.contains(var35)) {
                        var38 = "E1 loops are activated on the " + this.getInterfaceOfE1FromE1LoopsTable(var34) + "! Disable the loops on this E1 in order to continue.";
                        appContext.showErrorDialog("Configure Network Sync", var38);
                        this.refreshForm();
                        return false;
                     }
                  } else {
                     if(firstRow.length != 3) {
                        return false;
                     }

                     var34 = (MOID)firstRowSyncMode.get(NetSyncCallback.E1.E1_14TH);
                     var35 = MOIDHelper.Instance().getIdxString(var34);
                     if(errorMessage.contains(var35)) {
                        var38 = "E1 loops are activated on the " + this.getInterfaceOfE1FromE1LoopsTable(var34) + "! Disable the loops on this E1 in order to continue.";
                        appContext.showErrorDialog("Configure Network Sync", var38);
                        this.refreshForm();
                        return false;
                     }
                  }
               }
            }

            Map[] var26 = netSyncTable.getRows();
            var28 = var26[0];
            var29 = (AxxEnum)var28.get("SyncMode");
            String var30 = "The Quality Level should not be Not Assigned. Please choose another one!";
            String var32 = "Quality Level Warning";
            if(this.isInput(var29) && list[0].getAttribute("IsNotAssignedTheDefaultValue").getBooleanValue()) {
               AxxEnum var36 = (AxxEnum)list[0].getAttribute("SyncNomAssQualityLevel").getValue();
               if(var36.value() == 5) {
                  appContext.showWarningDialog(var30, var32, 3);
                  return false;
               }
            }
         }
      }

      return super.validate(appContext, list);
   }

   private String getInterfaceOfE1FromE1LoopsTable(MOID e1Moid) {
      try {
         return (String)MgtUtil.getInstance().getAttribute(e1Moid, "Interface");
      } catch (Exception var3) {
         log.error(var3);
         return null;
      }
   }

   private List getEnabledE1LoopsIndices(Map lastThreeE1s) {
      ArrayList enabledE1LoopIndices = new ArrayList();
      Iterator i$ = lastThreeE1s.keySet().iterator();

      while(i$.hasNext()) {
         NetSyncCallback.E1 key = (NetSyncCallback.E1)i$.next();
         MOID moid = (MOID)lastThreeE1s.get(key);
         if(this.isE1LoopEnabled(moid)) {
            enabledE1LoopIndices.add(MOIDHelper.Instance().getIdxString(moid));
         }
      }

      return enabledE1LoopIndices;
   }

   private Map getLastThreeE1FromE1LoopsTable(MOID neMoid) {
      HashMap lastThreeE1s = new HashMap();
      String pdhClass = "com.ericsson.mlcn.im.app.rl.pdh.PDH";
      MOID pdh = RelationSearchUtil.getChild(neMoid, pdhClass);
      if(pdh != null) {
         try {
            Object e = MgtUtil.getInstance().getAttribute(pdh, "E1LoopsTable");
            if(e != null && e instanceof TableRef) {
               TableRef tableRef = (TableRef)e;
               AxxMBTableMBean E1Table = (AxxMBTableMBean)MBeanProxy.createInstance(tableRef.getMoid(), MgtUtil.getInstance().getMgtService());
               MOID[] moids = E1Table.getRows();
               if(moids != null && moids.length > 3) {
                  Arrays.sort(moids);
                  MOID moid14th = moids[moids.length - 3];
                  lastThreeE1s.put(NetSyncCallback.E1.E1_14TH, moid14th);
                  MOID moid15th = moids[moids.length - 2];
                  lastThreeE1s.put(NetSyncCallback.E1.E1_15TH, moid15th);
                  MOID moid16th = moids[moids.length - 1];
                  lastThreeE1s.put(NetSyncCallback.E1.E1_16TH, moid16th);
               }
            }
         } catch (Exception var12) {
            log.error(var12);
         }
      }

      return lastThreeE1s;
   }

   private boolean isE1LoopEnabled(MOID E1Moid) {
      try {
         Boolean e = (Boolean)MgtUtil.getInstance().getAttribute(E1Moid, "LineLoop");
         Boolean localLoop = (Boolean)MgtUtil.getInstance().getAttribute(E1Moid, "LocalLoop");
         return e.booleanValue() || localLoop.booleanValue();
      } catch (Exception var4) {
         var4.printStackTrace();
         return false;
      }
   }

   private void refreshForm() {
      MLCraftFormViewer view = (MLCraftFormViewer)ApplicationManager.getInstance().getApplication(MLCraftFormViewer.class);
      view.refresh();
   }

   private boolean isInput(AxxEnum syncMode) {
      boolean isInput = syncMode != null && (SYNC_INPUT_2MHz.equals(syncMode.getValue()) || SYNC_INPUT_2Mbps.equals(syncMode.getValue()));
      return isInput;
   }

   private Map sortRows(Map[] allRows) {
      HashMap e1 = new HashMap();
      this.E1IndexToLabels = new HashMap();
      int size = allRows.length;
      String[] indices = new String[size];

      for(int i = 0; i < size; ++i) {
         Map row = allRows[i];
         String idx = MOIDHelper.Instance().getIdxString((MOID)row.get("Id"));
         String label = (String)row.get("Label");
         indices[i] = idx;
         this.E1IndexToLabels.put(idx, label);
      }

      Arrays.sort(indices);
      if(size == 3) {
         e1.put(NetSyncCallback.E1.E1_14TH, indices[0]);
         e1.put(NetSyncCallback.E1.E1_15TH, indices[1]);
         e1.put(NetSyncCallback.E1.E1_16TH, indices[2]);
      } else if(size == 2) {
         e1.put(NetSyncCallback.E1.E1_15TH, indices[0]);
         e1.put(NetSyncCallback.E1.E1_16TH, indices[1]);
      } else if(size == 1) {
         e1.put(NetSyncCallback.E1.E1_16TH, indices[0]);
      }

      return e1;
   }

   public void preSave(ApplicationContext appContext, MoAttributeList[] list) {
      if(list != null && list.length == 1) {
         MoAttributeList attrs = list[0];
         Attribute netSyncTableAtt = attrs.getAttribute("NetSyncTable");
         if(netSyncTableAtt != null) {
            AxxMBTableModel netSyncTable = (AxxMBTableModel)netSyncTableAtt.getValue();
            Map[] rows = netSyncTable.getRows();
            Map firstRow = null;
            Map[] viewer = rows;
            int formModel = rows.length;

            for(int syncForcedSwitchNode = 0; syncForcedSwitchNode < formModel; ++syncForcedSwitchNode) {
               Map syncNomAssQualityLevelNode = viewer[syncForcedSwitchNode];
               if(!netSyncTable.isRowDeleted(syncNomAssQualityLevelNode)) {
                  firstRow = syncNomAssQualityLevelNode;
                  break;
               }
            }

            MLCraftFormViewer var13 = (MLCraftFormViewer)appContext.getDesktopWindow();
            AxxFormModel var14 = var13.getModel();
            AxxFormNode var15 = var14.getNode("SyncForcedSwitch");
            AxxFormNode var16 = var14.getNode("SyncNomAssQualityLevel");
            if(firstRow != null) {
               AxxEnum firstRowSyncMode = (AxxEnum)firstRow.get("SyncMode");
               if(this.isInput(firstRowSyncMode)) {
                  attrs.removeAttribute("SyncForcedSwitch");
                  var15.setEdited(false);
               } else {
                  attrs.removeAttribute("SyncNomAssQualityLevel");
                  var16.setEdited(false);
               }
            } else {
               attrs.removeAttribute("SyncForcedSwitch");
               attrs.removeAttribute("SyncNomAssQualityLevel");
               var15.setEdited(false);
               var16.setEdited(false);
            }
         }
      }

      super.preSave(appContext, list);
   }


   private static enum E1 {

      E1_14TH("E1_14TH", 0),
      E1_15TH("E1_15TH", 1),
      E1_16TH("E1_16TH", 2);
      // $FF: synthetic field
      private static final NetSyncCallback.E1[] $VALUES = new NetSyncCallback.E1[]{E1_14TH, E1_15TH, E1_16TH};


      private E1(String var1, int var2) {}

   }
}
