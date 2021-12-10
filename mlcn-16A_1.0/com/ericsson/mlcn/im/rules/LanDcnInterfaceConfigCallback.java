package com.ericsson.mlcn.im.rules;

import com.ericsson.client.appl.MLCraftFormViewer;
import com.ericsson.client.appl.MLCraftFormViewerApplicationContext;
import com.ericsson.mlcraft.common.license.FormSaveCallback;
import no.axxessit.client.gui.ApplicationContext;
import no.axxessit.client.gui.forms.AxxFormNode;
import no.axxessit.client.util.MgtUtil;
import no.axxessit.common.lang.TranslationManager;
import no.axxessit.common.val.AxxEnum;
import no.axxessit.common.val.AxxIntConstraint;
import no.axxessit.common.val.AxxInteger;
import no.axxessit.mgt.Attribute;
import no.axxessit.mgt.AttributeList;
import no.axxessit.mgt.MOID;
import no.axxessit.mgt.MoAttributeList;
import no.axxessit.mgt.RID;

public class LanDcnInterfaceConfigCallback extends FormSaveCallback {

   public boolean validate(ApplicationContext appContext, MoAttributeList[] list) {
      Attribute defaultPrio = null;
      AxxEnum switchPortIndex = null;
      boolean validatePortRole = false;
      RID rid = null;

      for(int neMoid = 0; neMoid < list.length; ++neMoid) {
         if(list[neMoid].getMOID().getClassname().equals("com.ericsson.mlne.im.fa.ethernet.EthInterfaceStatusAndConfig")) {
            defaultPrio = list[neMoid].getAttribute("DefaultUserPriority");
            switchPortIndex = (AxxEnum)list[neMoid].getAttribute("PullDownInterfaceUsage").getValue();
            rid = list[neMoid].getResourceId();
         } else if(list[neMoid].getMOID().getClassname().equals("com.ericsson.mlne.im.fa.ethernet.LanDcnInterface")) {
            validatePortRole = list[neMoid].getAttribute("IsConnectedPortValidated").getBooleanValue();
         }
      }

      int oldSwitchPortIndex;
      if(defaultPrio != null && defaultPrio.getValue() != null) {
         AxxInteger var17 = (AxxInteger)defaultPrio.getValue();
         AxxIntConstraint e = (AxxIntConstraint)var17.getConstraint();
         oldSwitchPortIndex = (int)e.getMinValue();
         int viewer = (int)e.getMaxValue();
         if(!e.isLegal(var17.value())) {
            TranslationManager var26 = TranslationManager.getInstance();
            appContext.showErrorDialog(var26.getFormattedString("$ETHERNET.DEFAULT_USER_PRIORITY_ERROR", new Integer[]{new Integer(oldSwitchPortIndex), new Integer(viewer)}));
            return false;
         }
      }

      MOID var18 = MgtUtil.getInstance().getNe(list[0].getMOID());
      if(var18.getClassname().equals("com.ericsson.mlcn.im.MLCN")) {
         boolean var19 = false;

         try {
            var19 = ((Boolean)MgtUtil.getInstance().getMgtService().getAttribute(var18, "IsCNX10")).booleanValue();
         } catch (Exception var14) {
            ;
         }

         if(var19 && switchPortIndex != null && switchPortIndex.value() != 514 && switchPortIndex.value() != 513 && validatePortRole) {
            String var21 = "com.ericsson.mlne.im.fa.ethernet_bridge.SwitchPortsConfigAndStatus".substring("com.ericsson.mlne.im.fa.ethernet_bridge.SwitchPortsConfigAndStatus".lastIndexOf(".") + 1) + ".Switch Port:" + switchPortIndex.value();
            MOID var23 = new MOID("com.ericsson.mlne.im.fa.ethernet_bridge.SwitchPortsConfigAndStatus", var21, rid);
            AxxEnum interfaceUsageNode = null;
            AxxEnum accFrameTypes = null;

            try {
               interfaceUsageNode = (AxxEnum)MgtUtil.getInstance().getMgtService().getAttribute(var23, "PortRole");
               accFrameTypes = (AxxEnum)MgtUtil.getInstance().getMgtService().getAttribute(var23, "com.ericsson.mlne.im.fa.ethernet_bridge.SwitchPortConfig_UNI".substring("com.ericsson.mlne.im.fa.ethernet_bridge.SwitchPortConfig_UNI".lastIndexOf(".") + 1) + "." + "FrameTypeAdmit");
               if(interfaceUsageNode.value() != 0 && (interfaceUsageNode.value() != 1 && interfaceUsageNode.value() != 6 || accFrameTypes.value() != 3)) {
                  TranslationManager e1 = TranslationManager.getInstance();
                  appContext.showErrorDialog(e1.getFormattedString("$ETHERNET.LAN_DCN_PORTROLE_FRAMEADMIN_ERROR", new Integer[]{Integer.valueOf(switchPortIndex.value())}));
                  return false;
               }
            } catch (Exception var16) {
               ;
            }
         }
      }

      try {
         MOID var20 = null;

         for(oldSwitchPortIndex = 0; oldSwitchPortIndex < list.length; ++oldSwitchPortIndex) {
            if(list[oldSwitchPortIndex].getMOID().getClassname().equals("com.ericsson.mlne.im.fa.ethernet.EthInterfaceStatusAndConfig")) {
               var20 = list[oldSwitchPortIndex].getMOID();
            }
         }

         AxxEnum var22 = (AxxEnum)MgtUtil.getInstance().getMgtService().getAttribute(var20, "PullDownInterfaceUsage");
         if(switchPortIndex.compareTo(var22) == 0) {
            MLCraftFormViewer var24 = (MLCraftFormViewer)appContext.getDesktopWindow();
            AxxFormNode var25 = var24.getModel().getNode("PullDownInterfaceUsage");
            var25.setEdited(false);
         }
      } catch (Exception var15) {
         var15.printStackTrace();
      }

      return super.validate(appContext, list);
   }

   public void postSave(ApplicationContext appContext, MoAttributeList[] moList) {
      if(appContext != null && appContext instanceof MLCraftFormViewerApplicationContext) {
         MLCraftFormViewer formView = (MLCraftFormViewer)((MLCraftFormViewerApplicationContext)appContext).getDesktopWindow();

         for(int i = 0; i < moList.length; ++i) {
            if(moList[i].getAttribute("ConnectTo") != null) {
               AttributeList attList = moList[i].getAttributeList();
               moList[i] = new MoAttributeList(formView.getAddress().getMOID(), attList);
            }
         }
      }

      super.postSave(appContext, moList);
   }
}
