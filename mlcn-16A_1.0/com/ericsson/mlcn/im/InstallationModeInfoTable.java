package com.ericsson.mlcn.im;

import com.ericsson.mlcraft.valueconverter.LocalDateAndTimeValueConverter;
import com.ericsson.mlne.im.common.MrBean;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import no.axxessit.il.snmp.SnmpAdapter;
import no.axxessit.il.snmp.SnmpResource;
import no.axxessit.il.snmp.SnmpTable;
import no.axxessit.mgt.MBeanInfo;
import org.snmp4j.smi.OctetString;

public class InstallationModeInfoTable extends MrBean {

   private static final long serialVersionUID = -7282682048606494060L;
   public static final int FLASH_ROW_NUMBER = 1;
   public static final int RMM_ROW_NUMBER = 0;
   public static final int CT_COL_NUMBER = 1;
   public static final int DCNIP_COL_NUMBER = 2;
   public static final int DCNSN_COL_NUMBER = 3;
   public static final int PN_COL_NUMBER = 4;
   private SnmpTable installationModeInfoTable = null;
   private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


   public InstallationModeInfoTable(MBeanInfo info) {
      super(info);
   }

   public String getRMMConfigFileTime() {
      OctetString dateOctetString = new OctetString(this.getValueInTable(0, 1));
      byte[] b = dateOctetString.getValue();
      boolean allZero = true;

      for(int date = 0; date < b.length; ++date) {
         if(b[date] != 0) {
            allZero = false;
            break;
         }
      }

      if(!allZero) {
         Date var5 = (Date)(new LocalDateAndTimeValueConverter()).convertToJava(this.resource.getEquipmentType(), dateOctetString);
         return this.formatter.format(var5);
      } else {
         return "0000-00-00 00:00:00";
      }
   }

   public String getRMMDCNIP() {
      return this.getValueInTable(0, 2);
   }

   public String getRMMDCNSubNet() {
      return this.getValueInTable(0, 3);
   }

   public String getRMMProductNumber() {
      return this.getValueInTable(0, 4);
   }

   public String getFlashConfigFileTime() {
      OctetString dateOctetString = new OctetString(this.getValueInTable(1, 1));
      Date date = (Date)(new LocalDateAndTimeValueConverter()).convertToJava(this.resource.getEquipmentType(), dateOctetString);
      return this.formatter.format(date);
   }

   public String getFlashDCNIP() {
      return this.getValueInTable(1, 2);
   }

   public String getFlashDCNSubNet() {
      return this.getValueInTable(1, 3);
   }

   public String getFlashProductNumber() {
      return this.getValueInTable(1, 4);
   }

   private void loadInstallationModeInfoTable() {
      SnmpResource resource = (SnmpResource)this.context.getResource();
      SnmpAdapter snmpAdapter = resource.getAdapter();
      SnmpTable table = snmpAdapter.getTable("xfCNInstallationModeInfoEntry", new String[]{"xfCNInstallationModeInfoIndex", "xfCNInstallationModeConfigFileTime", "xfCNInstallationModeDCNIP", "xfCNInstallationModeDCNSubNet", "xfCNInstallationModeProductNumber"}, 2, (Object)null);
      this.installationModeInfoTable = table;
   }

   private String getValueInTable(int row, int column) {
      if(this.installationModeInfoTable == null) {
         this.loadInstallationModeInfoTable();
      }

      return (String)this.installationModeInfoTable.getValueAt(row, column);
   }
}
