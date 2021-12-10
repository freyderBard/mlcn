package com.ericsson.mlcn.im.rules;

import com.ericsson.client.appl.MLCraftFormViewer;
import com.ericsson.mlcraft.common.util.EnhancedAbstractDefaultRule;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.table.TableModel;
import no.axxessit.client.gui.ApplicationManager;
import no.axxessit.client.gui.forms.AxxFormContainer;
import no.axxessit.client.gui.forms.AxxFormNode;
import no.axxessit.client.gui.forms.elements.Attribute;
import no.axxessit.client.gui.forms.elements.Group;
import no.axxessit.client.gui.forms.elements.Table;
import no.axxessit.client.gui.table.AxxMBTableModel;
import no.axxessit.common.Env;
import no.axxessit.common.val.AxxEnum;
import no.axxessit.mgt.AttributeList;
import no.axxessit.mgt.MOID;
import no.axxessit.mgt.rule.inf.IContextProvider;

public class NetSyncInputOutputRule extends EnhancedAbstractDefaultRule {

   private static final String ATTR_NET_SYNC_TABLE = "NetSyncTable";
   private static final String ATTR_SYNC_MODE = "SyncMode";
   private static final String ATTR_SYNC_NOM_ASS_QUALITY_LEVEL = "SyncNomAssQualityLevel";
   private static final String ATTR_SYNC_NOM_QUALITY_LEVEL = "SyncNomQualityLevel";
   private static final String ATTR_SYNC_FORCED_SWITCH = "SyncForcedSwitch";
   private static final Integer SYNC_INPUT_2MHz = Integer.valueOf(2);
   private static final Integer SYNC_INPUT_2Mbps = Integer.valueOf(3);
   private static final Integer SYNC_OUTPUT_2Mbps = Integer.valueOf(5);
   private Table tableNetSync = null;
   private AxxFormNode nodeNetSync = null;
   private static final String ID_SYNC_STATUS_GROUP = "SyncStatusGroup";
   private Group groupSyncStatus = null;
   private Attribute attrSyncNomAssQualityLevel = null;
   private Attribute attrSyncNomQualityLevel = null;
   private Attribute attrSyncForcedSwitch = null;
   private MOID neMoid = null;


   public AttributeList evaluate(Object sourceContext, AttributeList list, IContextProvider callback, Object userData) {
      this.neMoid = (MOID)Env.getInstance().getObject("EQUIPMENT.MOID");
      MLCraftFormViewer view = (MLCraftFormViewer)ApplicationManager.getInstance().getApplication(MLCraftFormViewer.class);
      AxxFormContainer formContainer = view.getFormContainer();
      this.findComponents(formContainer);
      if(this.tableNetSync != null) {
         TableModel tm = this.tableNetSync.getTable().getModel();
         if(tm instanceof AxxMBTableModel) {
            AxxMBTableModel model = (AxxMBTableModel)tm;
            Map[] rows = model.getRows();
            if(rows.length > 0) {
               boolean isOutput = this.isOutput(model);
               if(isOutput) {
                  if(this.groupSyncStatus != null) {
                     this.groupSyncStatus.setVisible(true);
                  }

                  if(this.attrSyncNomAssQualityLevel != null) {
                     this.attrSyncNomAssQualityLevel.setVisible(false);
                  }

                  if(this.attrSyncNomQualityLevel != null) {
                     this.attrSyncNomQualityLevel.setVisible(true);
                  }

                  if(this.attrSyncForcedSwitch != null) {
                     this.attrSyncForcedSwitch.setVisible(true);
                  }
               } else {
                  if(this.groupSyncStatus != null) {
                     this.groupSyncStatus.setVisible(false);
                  }

                  if(this.attrSyncNomAssQualityLevel != null) {
                     this.attrSyncNomAssQualityLevel.setVisible(true);
                  }

                  if(this.attrSyncNomQualityLevel != null) {
                     this.attrSyncNomQualityLevel.setVisible(false);
                  }

                  if(this.attrSyncForcedSwitch != null) {
                     this.attrSyncForcedSwitch.setVisible(false);
                  }
               }
            }
         }
      }

      return null;
   }

   private boolean isOutput(AxxMBTableModel model) {
      boolean output = false;
      if(model != null) {
         Map[] rows = model.getRows();
         if(rows.length > 1) {
            output = true;
         } else if(rows.length == 1) {
            Map row = rows[0];
            AxxEnum syncMode = (AxxEnum)row.get("SyncMode");
            output = !this.isInputSyncMode(syncMode);
         }
      }

      return output;
   }

   private boolean isInputSyncMode(AxxEnum syncMode) {
      boolean isInput = syncMode != null && (SYNC_INPUT_2MHz.equals(syncMode.getValue()) || SYNC_INPUT_2Mbps.equals(syncMode.getValue()));
      return isInput;
   }

   private void findComponents(AxxFormContainer formContainer) {
      Collection comps = formContainer.getFormComponents();
      Iterator i$ = comps.iterator();

      while(i$.hasNext()) {
         JComponent comp = (JComponent)i$.next();
         AxxFormNode node;
         if(comp instanceof Attribute) {
            Attribute g = (Attribute)comp;
            node = formContainer.getNode(g);
            if(node != null) {
               if("SyncNomAssQualityLevel".equals(node.getId())) {
                  this.attrSyncNomAssQualityLevel = g;
               } else if("SyncNomQualityLevel".equals(node.getId())) {
                  this.attrSyncNomQualityLevel = g;
               } else if("SyncForcedSwitch".equals(node.getId())) {
                  this.attrSyncForcedSwitch = g;
               }
            }
         } else if(comp instanceof Table) {
            Table g1 = (Table)comp;
            node = formContainer.getNode(g1);
            if(node != null && "NetSyncTable".equals(node.getId())) {
               this.tableNetSync = g1;
               this.nodeNetSync = node;
            }
         } else if(comp instanceof Group) {
            Group g2 = (Group)comp;
            node = formContainer.getNode(g2);
            if(node != null && "SyncStatusGroup".equals(node.getId())) {
               this.groupSyncStatus = g2;
            }
         }
      }

   }

   public String getDestinationAttributeName() {
      return "SyncNomAssQualityLevel";
   }

   public String[] getSourceAttributeNames() {
      return new String[]{"SyncMode"};
   }

}
