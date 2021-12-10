package com.ericsson.mlcn.im.common.report;

import com.ericsson.mlcraft.im.common.report.ConfigurationReport;
import com.ericsson.mlne.utils.MMUUtil;
import no.axxessit.client.util.MgtUtil;
import no.axxessit.mgt.MBeanInfo;

public class MLCN_ConfigurationReport extends ConfigurationReport {

   private static final long serialVersionUID = -8693916637507395582L;


   public MLCN_ConfigurationReport(MBeanInfo info) {
      super(info);
   }

   public Boolean getEthernetDataSupported() {
      return Boolean.valueOf(!MMUUtil.isCNX10(MgtUtil.getInstance().getNe(this.moid)));
   }

   public Boolean getCNX10EthernetDataSupported() {
      return Boolean.valueOf(MMUUtil.isCNX10(MgtUtil.getInstance().getNe(this.moid)));
   }

   public Boolean getCNX10LanDataSupported() {
      return Boolean.valueOf(MMUUtil.isCNX10(MgtUtil.getInstance().getNe(this.moid)));
   }

   public Boolean getCNX10R2LicensesDataSupported() {
      return Boolean.valueOf(MMUUtil.isCNX10Release2(MgtUtil.getInstance().getNe(this.moid)));
   }
}
