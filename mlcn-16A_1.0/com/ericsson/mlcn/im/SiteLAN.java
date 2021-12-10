package com.ericsson.mlcn.im;

import com.ericsson.mlcraft.IMC;
import com.ericsson.mlcraft.common.util.BitsWrap;
import com.ericsson.mlne.im.common.MrBean;
import no.axxessit.common.val.AxxEnum;
import no.axxessit.im.common.ByteArray;
import no.axxessit.im.common.IPAddress;
import no.axxessit.im.util.IMUtil;
import no.axxessit.mgt.AttributeList;
import no.axxessit.mgt.MBeanException;
import no.axxessit.mgt.MBeanInfo;
import no.axxessit.mgt.MOID;
import no.axxessit.mgt.ManagementServer;
import no.axxessit.mgt.OperationException;

public class SiteLAN extends MrBean implements IMC {

   private static final long serialVersionUID = 7461518488909538009L;
   protected ManagementServer managementServer;


   public SiteLAN(MBeanInfo info) {
      super(info);
   }

   public IPAddress getIPAddress() {
      IPAddress ipAddress = new IPAddress();
      MOID neMoid = IMUtil.getInstance().getNe(this.moid);
      boolean isCNX10 = ((Boolean)this.getAttribute(neMoid, "IsCNX10")).booleanValue();
      if(isCNX10) {
         ipAddress = (IPAddress)this.getAttribute(neMoid, "IPAddress");
      } else {
         try {
            ipAddress = (IPAddress)this.resource.getAttribute(this.moid, "IPAddress");
         } catch (OperationException var5) {
            var5.printStackTrace();
         }
      }

      return ipAddress;
   }

   public void setSubnetMask(IPAddress address) {
      this.resource.setAttribute(this.moid, "SubnetMask", address);
      this.wait3Seconds();
   }

   public void setSpeed(AxxEnum speed) {
      this.resource.setAttribute(this.moid, "Speed", Integer.valueOf(speed.value()));
      this.wait3Seconds();
   }

   public void setMdiMdix(AxxEnum mdiMdx) {
      this.resource.setAttribute(this.moid, "MdiMdix", Integer.valueOf(mdiMdx.value()));
      this.wait3Seconds();
   }

   public void setAutoNegotiate(AxxEnum autoNegotiate) {
      this.resource.setAttribute(this.moid, "AutoNegotiate", Integer.valueOf(autoNegotiate.value()));
      this.wait3Seconds();
   }

   private void wait3Seconds() {
      MOID neMoid = IMUtil.getInstance().getNe(this.moid);
      boolean isCNX10 = ((Boolean)this.getAttribute(neMoid, "IsCNX10")).booleanValue();
      if(!isCNX10) {
         try {
            Thread.sleep(3000L);
         } catch (InterruptedException var4) {
            log.error(var4);
         }

      }
   }

   public AttributeList setAttributes(AttributeList attributeList) throws MBeanException, OperationException {
      AttributeList atts = (AttributeList)attributeList.clone();
      if(atts.getAttribute("AutoNegotiate") != null && atts.getAttribute("Speed") != null) {
         super.setAttribute(atts.getAttribute("AutoNegotiate"));
         atts.removeAllAttributes("AutoNegotiate");
         AxxEnum speed = (AxxEnum)atts.getAttribute("Speed").getValue();
         if(speed.value() == 1) {
            atts.removeAllAttributes("Speed");
         }
      }

      return super.setAttributes(atts);
   }

   public Boolean getIsSiteLANNotificationsVisible() {
      ByteArray siteLanCapability = (ByteArray)this.resource.getAttribute(this.moid, "SiteLanCapability", true);
      return siteLanCapability != null?Boolean.valueOf(BitsWrap.getBitValue(siteLanCapability.byteValue(), siteLanCapability.byteValue().length * 8 - 1)):Boolean.valueOf(false);
   }
}
