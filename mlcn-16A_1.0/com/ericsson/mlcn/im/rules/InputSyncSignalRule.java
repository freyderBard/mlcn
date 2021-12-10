package com.ericsson.mlcn.im.rules;

import com.ericsson.client.appl.MLCraftFormViewer;
import java.util.Map;
import no.axxessit.client.gui.ApplicationManager;
import no.axxessit.client.gui.forms.nodes.Table;
import no.axxessit.client.gui.table.AxxMBTableModel;
import no.axxessit.common.val.AxxEnum;
import no.axxessit.mgt.Attribute;
import no.axxessit.mgt.AttributeList;
import no.axxessit.mgt.MOID;
import no.axxessit.mgt.rule.AbstractDefaultRule;
import no.axxessit.mgt.rule.inf.IContextProvider;

public class InputSyncSignalRule extends AbstractDefaultRule {

   private static final String ATTR_SYNC_MODE = "SyncMode";
   private static final String WARNING_GROUP = "warninggroup";
   private static final String ATTR_QUALITY_LEVEL = "SyncNomAssQualityLevel";
   private static final String ATTR_IS_NOT_ASSIGNED_DEFAULT_VALUE = "IsNotAssignedTheDefaultValue";
   private static final String ATTR_NET_SYNC_TABLE = "NetSyncTable";
   private static final Integer SYNC_INPUT_2MHz = Integer.valueOf(2);
   private static final Integer SYNC_INPUT_2Mbps = Integer.valueOf(3);


   public AttributeList evaluate(Object sourceContext, AttributeList list, IContextProvider callback, Object userData) {
      MOID moid = (MOID)sourceContext;
      if(list != null) {
         Attribute qualityLevelAttr = list.getAttribute("SyncNomAssQualityLevel");
         Attribute isNotAssignedTheDefaultValueAttr = list.getAttribute("IsNotAssignedTheDefaultValue");
         if(qualityLevelAttr == null || qualityLevelAttr.getValue() == null || isNotAssignedTheDefaultValueAttr == null || isNotAssignedTheDefaultValueAttr.getValue() == null) {
            return null;
         }

         AxxEnum qualityLevel = (AxxEnum)qualityLevelAttr.getValue();
         boolean isNotAssignedTheDefaultValue = ((Boolean)isNotAssignedTheDefaultValueAttr.getValue()).booleanValue();
         boolean result = false;
         if(isNotAssignedTheDefaultValue && 5 == qualityLevel.value()) {
            MLCraftFormViewer formViewer = (MLCraftFormViewer)((MLCraftFormViewer)ApplicationManager.getInstance().getApplication("MLCraftFormViewer"));
            Table netSyncTableNode = (Table)formViewer.getModel().getNode("NetSyncTable");
            AxxMBTableModel tableModel = (AxxMBTableModel)netSyncTableNode.getValue();
            Map firstRow = tableModel.getRow(0);
            AxxEnum syncMode = (AxxEnum)firstRow.get("SyncMode");
            result = null != syncMode && this.isInput(syncMode);
         }

         this.propertyVisibilityChange(moid, result, (Object)null);
      }

      return null;
   }

   public String getDestinationAttributeName() {
      return "warninggroup";
   }

   public String[] getSourceAttributeNames() {
      return new String[]{"IsNotAssignedTheDefaultValue", "SyncNomAssQualityLevel"};
   }

   private boolean isInput(AxxEnum syncMode) {
      boolean isInput = syncMode != null && (SYNC_INPUT_2MHz.equals(syncMode.getValue()) || SYNC_INPUT_2Mbps.equals(syncMode.getValue()));
      return isInput;
   }

}
