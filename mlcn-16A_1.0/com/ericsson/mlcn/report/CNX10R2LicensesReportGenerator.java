package com.ericsson.mlcn.report;

import com.ericsson.mlcraft.report.AbstractReportGenerator;
import com.ericsson.mlcraft.report.IReportValueConverter;
import com.ericsson.mlne.im.common.licenses.Licenses;
import no.axxessit.mgt.AttRef;
import no.axxessit.mgt.AxxMBTableMBean;
import no.axxessit.mgt.MBeanException;
import no.axxessit.mgt.MBeanProxy;
import no.axxessit.mgt.MOID;
import no.axxessit.mgt.OperationException;
import no.axxessit.mgt.TableRef;

public class CNX10R2LicensesReportGenerator extends AbstractReportGenerator {

   public CNX10R2LicensesReportGenerator(MOID rootMoid) {
      super(rootMoid);
   }

   public String getReport() {
      StringBuffer reportContents = new StringBuffer();

      try {
         MOID e = new MOID(Licenses.class.getName(), "Licenses", this.rootMoid.getResourceId());
         AttRef att = new AttRef(e);
         TableRef tableRef = (TableRef)mgtServer.getAttribute(att.getMoid(), "LicenseFeatureTable");
         AxxMBTableMBean snmpManagerTable = (AxxMBTableMBean)MBeanProxy.createInstance(tableRef.getMoid(), mgtServer);
         MOID[] snmpManagerRows = snmpManagerTable.getRows();
         this.appendNewLine(reportContents, "<CNX10R2LicensesConfiguration>");

         for(int i = 0; i < snmpManagerRows.length; ++i) {
            this.appendNewLine(reportContents, "<CNX10R2Licenses>");
            this.appendReportElement(reportContents, snmpManagerRows[i], "Number", "Number", (IReportValueConverter)null);
            this.appendReportElement(reportContents, snmpManagerRows[i], "Name", "Name", (IReportValueConverter)null);
            this.appendReportElement(reportContents, snmpManagerRows[i], "Quantity", "Installed", (IReportValueConverter)null);
            this.appendReportElement(reportContents, snmpManagerRows[i], "Subscribed", "Used", (IReportValueConverter)null);
            this.appendNewLine(reportContents, "</CNX10R2Licenses>");
         }

         this.appendNewLine(reportContents, "</CNX10R2LicensesConfiguration>");
      } catch (MBeanException var8) {
         log.error(var8);
      } catch (OperationException var9) {
         log.error(var9);
      } catch (ClassNotFoundException var10) {
         log.error(var10);
      }

      return reportContents.toString();
   }
}
