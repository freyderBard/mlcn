package com.ericsson.mlcn.im;

import com.ericsson.mlne.im.common.MrBean;
import no.axxessit.mgt.MBeanInfo;

public class NetSyncTable extends MrBean {

   private static final long serialVersionUID = 3206586916389370649L;
   private static final String MOC_ATTR = "Moc";
   private static final String MOI_ATTR = "Moi";


   public NetSyncTable(MBeanInfo info) {
      super(info);
   }

   public String getLabel() {
      return (String)this.resource.getAttribute(this.moid, "Moc") + " " + (String)this.resource.getAttribute(this.moid, "Moi");
   }
}
