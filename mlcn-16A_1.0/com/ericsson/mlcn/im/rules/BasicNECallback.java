package com.ericsson.mlcn.im.rules;

import com.ericsson.mlcn.im.rules.NECallback;
import com.ericsson.mlne.im.rules.BasicSettingsTimeHelper;
import com.ericsson.mlne.im.rules.WarningException;
import no.axxessit.client.gui.ApplicationContext;
import no.axxessit.mgt.MBeanException;
import no.axxessit.mgt.MoAttributeList;

public class BasicNECallback extends NECallback {

   public void postSave(ApplicationContext appContext, MoAttributeList[] moList) {
      super.postSave(appContext, moList);
      BasicSettingsTimeHelper.rebootIfTimeOrTimeZoneIsEdited(appContext, moList);
   }

   void customValidate(ApplicationContext appContext, MoAttributeList[] list) throws NECallback.CheckFailedException, MBeanException, WarningException {
      MoAttributeList eqRoot = null;
      int len$ = list.length;
      byte i$ = 0;
      if(i$ < len$) {
         MoAttributeList object = list[i$];
         if(object.getObjectId().toString().equals("eqroot")) {
            eqRoot = object;
         }
      }

      this.checkNEName(appContext, eqRoot);
      this.checkNEContact(appContext, eqRoot);
      this.checkNEIPAddressAndSubnetMask(appContext, eqRoot);
      this.checkDefaultGateway(appContext, eqRoot);
      this.checkNTPServer(appContext, eqRoot);
      this.checkSNMPTrapReceivers(appContext, eqRoot);
      this.checkAlarmFilterTime(appContext, eqRoot);
      this.checkPMStartTime(appContext, eqRoot);
      this.checkDHCPAddress(appContext, eqRoot);
      this.checkContinentAndCity(appContext, eqRoot);
      this.checkNELocation(appContext, eqRoot);
      BasicSettingsTimeHelper.warnUserAboutTimeAndTimeZone(appContext);
   }
}
