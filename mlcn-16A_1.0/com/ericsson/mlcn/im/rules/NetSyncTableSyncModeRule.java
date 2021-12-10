package com.ericsson.mlcn.im.rules;

import com.ericsson.client.appl.MLCraftFormViewer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import no.axxessit.client.gui.ApplicationManager;
import no.axxessit.client.gui.forms.AxxFormContainer;
import no.axxessit.client.gui.forms.AxxFormModel;
import no.axxessit.client.gui.forms.AxxFormNode;
import no.axxessit.client.gui.forms.elements.Table;
import no.axxessit.client.gui.table.AxxMBTableModel;
import no.axxessit.client.util.MgtUtil;
import no.axxessit.common.Env;
import no.axxessit.common.val.AxxEnum;
import no.axxessit.common.val.AxxEnumConstraint;
import no.axxessit.common.val.AxxValueConstraint;
import no.axxessit.mgt.Attribute;
import no.axxessit.mgt.AttributeList;
import no.axxessit.mgt.MOID;
import no.axxessit.mgt.rule.AbstractDefaultRule;
import no.axxessit.mgt.rule.event.AttributeValueChangeEvent;
import no.axxessit.mgt.rule.inf.IContextProvider;

public class NetSyncTableSyncModeRule extends AbstractDefaultRule {

   private static final String ATTR_CN_SYNC_BOARD_CAPABILITY = "CNSyncBoardCapability";
   private static final String ATTR_SYNC_MODE = "SyncMode";
   private static final String ATTR_NET_SYNC_TABLE = "NetSyncTable";
   private static final String CONSTRAINT_SYNC_MODE = "SYNC_MODE";
   private static final Integer PDH_TRAFFIC = Integer.valueOf(1);
   private static final Integer SYNC_INPUT_2MHz = Integer.valueOf(2);
   private static final Integer SYNC_INPUT_2Mbps = Integer.valueOf(3);
   private static final Integer SYNC_OUTPUT_2MHz = Integer.valueOf(4);
   private static final Integer SYNC_OUTPUT_2Mbps = Integer.valueOf(5);
   private static final Integer SYNC_BOARD_CAP_NO = Integer.valueOf(0);
   private static final Integer SYNC_BOARD_CAP_2Mbps = Integer.valueOf(1);
   private static final Integer SYNC_BOARD_CAP_2MHz = Integer.valueOf(2);
   private static final Integer SYNC_BOARD_CAP_2Mbps_AND_2MHz = Integer.valueOf(3);
   private Map existingRows = new HashMap();


   public AttributeList evaluate(Object sourceContext, AttributeList list, IContextProvider callback, Object userData) {
      AxxEnum syncCap = this.getSyncBoardCapability();
      if(callback != null && callback instanceof AxxMBTableModel && syncCap != null && !SYNC_BOARD_CAP_NO.equals(syncCap.getValue())) {
         AxxMBTableModel netSyncTableModel = (AxxMBTableModel)callback;
         Attribute id = list.getAttribute("Id");
         Attribute syncModeAttr = list.getAttribute("SyncMode");
         AxxEnum syncMode = (AxxEnum)syncModeAttr.getValue();
         if(id != null && id.getValue() != null) {
            this.existingRows.put((MOID)id.getValue(), syncMode);
         }

         this.controlSyncModeOptionForAddOperation(syncMode, netSyncTableModel, userData, syncCap);
         this.controlSyncModeOptionForEditOperation(netSyncTableModel, userData, syncCap);
         this.controlAddButton(netSyncTableModel);
         MLCraftFormViewer formViewer = (MLCraftFormViewer)((MLCraftFormViewer)ApplicationManager.getInstance().getApplication("MLCraftFormViewer"));
         formViewer.getModel().evaluateRules();
      }

      return null;
   }

   private void controlAddButton(AxxMBTableModel netSyncTableModel) {
      if(!this.existingRows.isEmpty() && this.existingRows.size() == 1) {
         Iterator vals = this.existingRows.values().iterator();
         AxxEnum val = (AxxEnum)vals.next();
         this.controlAddButtonVisibility(!this.isInput(val) && !netSyncTableModel.isEdited());
      }

   }

   private void controlSyncModeOptionForEditOperation(AxxMBTableModel netSyncTableModel, Object userData, AxxEnum syncCap) {
      MOID[] moidsArray = new MOID[this.existingRows.size()];
      this.existingRows.keySet().toArray(moidsArray);
      AxxEnumConstraint constraint;
      String[] labels;
      String[] newSyncMode;
      int changeEvent;
      int i$;
      String label;
      int val;
      if(moidsArray.length == 1) {
         MOID i = moidsArray[0];
         AxxEnum moid = (AxxEnum)this.existingRows.get(i);
         AxxEnumConstraint syncMode = (AxxEnumConstraint)moid.getConstraint();
         constraint = new AxxEnumConstraint("SYNC_MODE");
         boolean newConstraint = netSyncTableModel.getAddedRows().size() > 0;
         if(newConstraint) {
            labels = syncMode.getLabels();
            newSyncMode = labels;
            changeEvent = labels.length;

            for(i$ = 0; i$ < changeEvent; ++i$) {
               label = newSyncMode[i$];
               val = syncMode.getValue(label);
               if(SYNC_OUTPUT_2Mbps.intValue() == val && this.is2MbpsSupported(syncCap) || SYNC_OUTPUT_2MHz.intValue() == val && this.is2MHzSupported(syncCap)) {
                  constraint.addValue(val, label, (String)null);
               }
            }
         } else {
            labels = syncMode.getLabels();
            newSyncMode = labels;
            changeEvent = labels.length;

            for(i$ = 0; i$ < changeEvent; ++i$) {
               label = newSyncMode[i$];
               val = syncMode.getValue(label);
               if((SYNC_OUTPUT_2Mbps.intValue() == val || SYNC_INPUT_2Mbps.intValue() == val) && this.is2MbpsSupported(syncCap) || (SYNC_OUTPUT_2MHz.intValue() == val || SYNC_INPUT_2MHz.intValue() == val) && this.is2MHzSupported(syncCap)) {
                  constraint.addValue(val, label, (String)null);
               }
            }
         }

         if(!syncMode.toString().equals(constraint.toString())) {
            AxxEnum var20 = new AxxEnum(moid.value(), constraint);
            AttributeValueChangeEvent var21 = new AttributeValueChangeEvent(i, "SyncMode", (Object)null, var20);
            var21.setUserData(userData);
            this.firePropertyChange(var21);
         }
      } else {
         if(this.existingRows.size() != 2 && moidsArray.length != 3) {
            return;
         }

         for(int var16 = 0; var16 < moidsArray.length; ++var16) {
            MOID var17 = moidsArray[var16];
            AxxEnum var18 = (AxxEnum)this.existingRows.get(var17);
            constraint = (AxxEnumConstraint)var18.getConstraint();
            AxxEnumConstraint var19 = new AxxEnumConstraint("SYNC_MODE");
            labels = constraint.getLabels();
            newSyncMode = labels;
            changeEvent = labels.length;

            for(i$ = 0; i$ < changeEvent; ++i$) {
               label = newSyncMode[i$];
               val = constraint.getValue(label);
               if(SYNC_OUTPUT_2Mbps.intValue() == val && this.is2MbpsSupported(syncCap) || SYNC_OUTPUT_2MHz.intValue() == val && this.is2MHzSupported(syncCap)) {
                  var19.addValue(val, label, (String)null);
               }
            }

            if(!constraint.toString().equals(var19.toString())) {
               AxxEnum var22 = new AxxEnum(var18.value(), var19);
               AttributeValueChangeEvent var23 = new AttributeValueChangeEvent(var17, "SyncMode", (Object)null, var22);
               var23.setUserData(userData);
               this.firePropertyChange(var23);
            }
         }
      }

   }

   private void controlSyncModeOptionForAddOperation(AxxEnum syncMode, AxxMBTableModel netSyncTableModel, Object userData, AxxEnum syncCap) {
      AxxValueConstraint syncModeConstraint = syncMode.getConstraint();
      AxxEnumConstraint syncModeEnumConstraint = (AxxEnumConstraint)syncModeConstraint;
      AxxEnumConstraint newSyncModeConstraint = new AxxEnumConstraint("SYNC_MODE");
      String[] labels = syncModeEnumConstraint.getLabels();
      String[] newLabels = labels;
      int addedRowsArray = labels.length;

      int i;
      for(i = 0; i < addedRowsArray; ++i) {
         String row = newLabels[i];
         if(PDH_TRAFFIC.intValue() != syncModeEnumConstraint.getValue(row)) {
            newSyncModeConstraint.addValue(syncModeEnumConstraint.getValue(row), row, (String)null);
         }
      }

      if(this.existingRows.isEmpty() && PDH_TRAFFIC.equals(syncMode.getValue())) {
         if(this.is2MHzSupported(syncCap)) {
            syncMode.setValue(SYNC_INPUT_2MHz);
         } else {
            syncMode.setValue(SYNC_INPUT_2Mbps);
         }
      }

      if(!this.existingRows.isEmpty()) {
         boolean var15 = false;
         Iterator var16 = this.existingRows.values().iterator();
         if(var16.hasNext()) {
            AxxEnum var19 = (AxxEnum)var16.next();
            var15 = this.isInput(var19);
         }

         if(var15) {
            if(newSyncModeConstraint.containsValue(SYNC_OUTPUT_2MHz.intValue())) {
               newSyncModeConstraint.removeValue(SYNC_OUTPUT_2MHz.intValue());
            }

            if(newSyncModeConstraint.containsValue(SYNC_OUTPUT_2Mbps.intValue())) {
               newSyncModeConstraint.removeValue(SYNC_OUTPUT_2Mbps.intValue());
            }

            if(!SYNC_INPUT_2Mbps.equals(syncMode.getValue()) && !SYNC_INPUT_2MHz.equals(syncMode.getValue())) {
               if(this.is2MHzSupported(syncCap)) {
                  syncMode.setValue(SYNC_INPUT_2MHz);
               } else {
                  syncMode.setValue(SYNC_INPUT_2Mbps);
               }
            } else if(SYNC_INPUT_2MHz.equals(syncMode.getValue()) && !this.is2MHzSupported(syncCap)) {
               syncMode.setValue(SYNC_INPUT_2Mbps);
            }
         } else {
            if(newSyncModeConstraint.containsValue(SYNC_INPUT_2MHz.intValue())) {
               newSyncModeConstraint.removeValue(SYNC_INPUT_2MHz.intValue());
            }

            if(newSyncModeConstraint.containsValue(SYNC_INPUT_2Mbps.intValue())) {
               newSyncModeConstraint.removeValue(SYNC_INPUT_2Mbps.intValue());
            }

            if(!SYNC_OUTPUT_2Mbps.equals(syncMode.getValue()) && !SYNC_OUTPUT_2MHz.equals(syncMode.getValue())) {
               if(this.is2MHzSupported(syncCap)) {
                  syncMode.setValue(SYNC_OUTPUT_2MHz);
               } else {
                  syncMode.setValue(SYNC_OUTPUT_2Mbps);
               }
            } else if(SYNC_OUTPUT_2MHz.equals(syncMode.getValue()) && !this.is2MHzSupported(syncCap)) {
               syncMode.setValue(SYNC_OUTPUT_2Mbps);
            }
         }
      }

      newLabels = newSyncModeConstraint.getLabels();
      String[] var17 = newLabels;
      i = newLabels.length;

      for(int var20 = 0; var20 < i; ++var20) {
         String newSyncMode = var17[var20];
         int changeEvent = newSyncModeConstraint.getValue(newSyncMode);
         if(!this.is2MHzSupported(syncCap) && (SYNC_INPUT_2MHz.intValue() == changeEvent || SYNC_OUTPUT_2MHz.intValue() == changeEvent)) {
            newSyncModeConstraint.removeValue(changeEvent);
         }

         if(!this.is2MbpsSupported(syncCap) && (SYNC_INPUT_2Mbps.intValue() == changeEvent || SYNC_OUTPUT_2Mbps.intValue() == changeEvent)) {
            newSyncModeConstraint.removeValue(changeEvent);
         }
      }

      if(!syncModeConstraint.toString().equals(newSyncModeConstraint.toString())) {
         Object[] var18 = netSyncTableModel.getAddedRows().entrySet().toArray();

         for(i = 0; i < var18.length; ++i) {
            Map var21 = (Map)((Entry)var18[i]).getKey();
            AxxEnum var22 = new AxxEnum(syncMode.value(), newSyncModeConstraint);
            AttributeValueChangeEvent var23 = new AttributeValueChangeEvent(var21, "SyncMode", (Object)null, var22);
            var23.setUserData(userData);
            this.firePropertyChange(var23);
         }
      }

   }

   private void controlAddButtonVisibility(boolean visible) {
      MLCraftFormViewer view = (MLCraftFormViewer)ApplicationManager.getInstance().getApplication(MLCraftFormViewer.class);
      AxxFormContainer formContainer = view.getFormContainer();
      AxxFormModel formModel = formContainer.getModel();
      AxxFormNode formNode = formModel.getNode("NetSyncTable");
      if(formNode != null) {
         Table comp = (Table)formContainer.getComponent(formNode);
         comp.setAddVisible(visible);
      }

   }

   private void updateConstraintByCNSyncBoardCapability(AxxEnumConstraint constraint) {
      MOID neMoid = (MOID)Env.getInstance().getObject("EQUIPMENT.MOID");
   }

   private AxxEnum getSyncBoardCapability() {
      MOID neMoid = (MOID)Env.getInstance().getObject("EQUIPMENT.MOID");

      try {
         Object e = MgtUtil.getInstance().getAttribute(neMoid, "CNSyncBoardCapability");
         if(e != null && e instanceof AxxEnum) {
            return (AxxEnum)e;
         }
      } catch (Exception var3) {
         var3.printStackTrace();
      }

      return null;
   }

   private boolean is2MHzSupported(AxxEnum syncCap) {
      return syncCap != null && (SYNC_BOARD_CAP_2MHz.equals(syncCap.getValue()) || SYNC_BOARD_CAP_2Mbps_AND_2MHz.equals(syncCap.getValue()));
   }

   private boolean is2MbpsSupported(AxxEnum syncCap) {
      return syncCap != null && (SYNC_BOARD_CAP_2Mbps.equals(syncCap.getValue()) || SYNC_BOARD_CAP_2Mbps_AND_2MHz.equals(syncCap.getValue()));
   }

   private boolean isInput(AxxEnum syncMode) {
      boolean isInput = syncMode != null && (SYNC_INPUT_2MHz.equals(syncMode.getValue()) || SYNC_INPUT_2Mbps.equals(syncMode.getValue()));
      return isInput;
   }

   public String getDestinationAttributeName() {
      return "SyncMode";
   }

   public String[] getSourceAttributeNames() {
      return new String[]{"SyncMode"};
   }

}
