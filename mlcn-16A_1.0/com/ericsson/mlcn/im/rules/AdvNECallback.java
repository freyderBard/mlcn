package com.ericsson.mlcn.im.rules;

import com.ericsson.mlcn.im.rules.NECallback;
import no.axxessit.client.gui.ApplicationContext;
import no.axxessit.client.util.MgtUtil;
import no.axxessit.common.val.AxxEnum;
import no.axxessit.mgt.Attribute;
import no.axxessit.mgt.MBeanException;
import no.axxessit.mgt.MOID;
import no.axxessit.mgt.MoAttributeList;
import org.apache.log4j.Logger;

public class AdvNECallback extends NECallback {

   private static Logger logger = Logger.getLogger(AdvNECallback.class);


   void customValidate(ApplicationContext appContext, MoAttributeList[] list) throws NECallback.CheckFailedException, MBeanException {
      MoAttributeList eqRoot = null;
      MoAttributeList siteLANAttr = null;
      MoAttributeList ftpConfigAttr = null;
      MoAttributeList[] isCNX10 = list;
      int len$ = list.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         MoAttributeList object = isCNX10[i$];
         if(object.getObjectId().toString().equals("eqroot")) {
            eqRoot = object;
         } else if(object.getMOID().getClassname().equals("com.ericsson.mlcn.im.SiteLAN")) {
            siteLANAttr = object;
         } else if(object.getObjectId().toString().equals("FtpConfig.eqroot")) {
            ftpConfigAttr = object;
         }
      }

      if(ftpConfigAttr.getClassname().equals("com.ericsson.mlne.im.FtpConfig")) {
         this.checkRemoteFTPServer(appContext, ftpConfigAttr);
      }

      this.checkStaticRoutes(appContext, eqRoot);
      Boolean var10 = Boolean.valueOf(eqRoot.getAttribute("IsCNX10").getBooleanValue());
      if(var10.booleanValue()) {
         this.checkSpeedTowardsAutoNeg(appContext, siteLANAttr);
      } else {
         this.checkLANIPAddressAndSubnetMask(appContext, siteLANAttr, eqRoot);
         this.checkSpeedTowardsAutoNeg(appContext, siteLANAttr);
         this.checkDefaultGateway(appContext, eqRoot);
      }

   }

   protected void checkSpeedTowardsAutoNeg(ApplicationContext appContext, MoAttributeList eqRoot) {
      Attribute autoNeg = eqRoot.getAttribute("AutoNegotiate");
      Attribute speed = eqRoot.getAttribute("Speed");
      if(autoNeg != null && speed != null && ((AxxEnum)speed.getValue()).value() == 1 && ((AxxEnum)autoNeg.getValue()).value() == 1) {
         MOID siteLanMoid = eqRoot.getMOID();

         try {
            AxxEnum e = new AxxEnum(1, "ON_OFF");
            AxxEnum speedEnum = new AxxEnum(1, "SITE_LAN_SPEED");
            MgtUtil.getInstance().setAttribute(siteLanMoid, "AutoNegotiate", e);
            MgtUtil.getInstance().setAttribute(siteLanMoid, "Speed", speedEnum);
         } catch (Exception var8) {
            logger.error(var8);
         }
      }

   }

}
