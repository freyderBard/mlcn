package com.ericsson.mlcn.report;

import com.ericsson.mlcraft.report.AbstractReportGenerator;
import com.ericsson.mlcraft.report.IReportValueConverter;
import com.ericsson.mlcraft.report.AbstractReportGenerator.BooleanToIfAdminStatusConverter;
import com.ericsson.mlne.utils.MMUUtil;
import no.axxessit.client.util.GUIAttRef;
import no.axxessit.client.util.MgtUtil;
import no.axxessit.common.val.AxxEnum;
import no.axxessit.mgt.AttRef;
import no.axxessit.mgt.AxxMBTableMBean;
import no.axxessit.mgt.MBeanException;
import no.axxessit.mgt.MBeanProxy;
import no.axxessit.mgt.MOID;
import no.axxessit.mgt.MOIDList;
import no.axxessit.mgt.OperationException;
import no.axxessit.mgt.TableRef;

public class E1ReportGenerator extends AbstractReportGenerator {

   public E1ReportGenerator(MOID rootMoid) {
      super(rootMoid);
   }

   public String getReport() {
      StringBuffer reportContents = new StringBuffer();
      MOID tempMOID = new MOID("com.ericsson.mlcn.im.app.rl.pdh.PDH", "*", this.rootMoid.getResourceId());
      MOIDList tempMoids = mgtServer.queryMBeans(tempMOID);
      MOID pdh = null;
      if(tempMoids.size() > 0) {
         pdh = (MOID)tempMoids.get(0);

         try {
            MOID e = ((AttRef)mgtServer.getAttribute(pdh, "X1Config")).getMoid();
            String inputImpedance = this.getInputImpedance(e);
            TableRef tableRef = (TableRef)mgtServer.getAttribute(e, "X1ConfigTable");
            AxxMBTableMBean x1ConfigTable = (AxxMBTableMBean)MBeanProxy.createInstance(tableRef.getMoid(), mgtServer);
            MOID[] e1Rows = x1ConfigTable.getRows();
            this.appendNewLine(reportContents, "<E1Configuration>");

            for(int i = 0; i < e1Rows.length; ++i) {
               this.appendNewLine(reportContents, "<E1>");
               this.appendReportElement(reportContents, e1Rows[i], "Label", (String)null, (IReportValueConverter)null);
               this.appendReportElement(reportContents, e1Rows[i], "AdminStatus", (String)null, new BooleanToIfAdminStatusConverter(this));
               this.appendReportElement(reportContents, "E1InputImpedance", inputImpedance);
               this.appendNewLine(reportContents, "</E1>");
            }

            this.appendNewLine(reportContents, "</E1Configuration>");
            this.addMoreReportIfNeeded(reportContents);
         } catch (MBeanException var11) {
            log.error(var11);
         } catch (OperationException var12) {
            log.error(var12);
         } catch (ClassNotFoundException var13) {
            log.error(var13);
         }

         return reportContents.toString();
      } else {
         return null;
      }
   }

   private void addMoreReportIfNeeded(StringBuffer reportContents) {
      if(this.isCN710(this.rootMoid) || this.isCN810(this.rootMoid) || this.isCN810_R2(this.rootMoid)) {
         this.getTrafficRoutingTable(reportContents);
         this.getSCNPTable(reportContents);
      }

   }

   private void getSCNPTable(StringBuffer reportContents) {
      MOID sncpMoid = new MOID("com.ericsson.mlne.im.fa.traffic_routing.SNCP", "SNCP", this.rootMoid.getResourceId());
      AxxMBTableMBean sncpTable = MMUUtil.getAxxMBTable(sncpMoid, "SNCPTable");
      if(null != sncpTable) {
         this.appendNewLine(reportContents, "<SNCPConfiguration>");
         MOID[] areaRows = sncpTable.getRows();

         for(int i = 0; i < areaRows.length; ++i) {
            MOID temp = areaRows[i];
            this.appendNewLine(reportContents, "<SNCP>");
            this.appendReportElement(reportContents, temp, "Interface1", "Interface1", new E1ReportGenerator.X1OverviewInterfaceConverter("MocMoi"));
            this.appendReportElement(reportContents, temp, "Interface2", "Interface2", new E1ReportGenerator.X1OverviewInterfaceConverter("MocMoi"));
            this.appendReportElement(reportContents, temp, "SwitchMode", "SwitchMode", (IReportValueConverter)null);
            this.appendReportElement(reportContents, temp, "HoldOffTime", "HoldOff", (IReportValueConverter)null);
            this.appendReportElement(reportContents, temp, "ActiveInterface", "ActiveInterface", (IReportValueConverter)null);
            this.appendReportElement(reportContents, temp, "SwitchCount", "SwitchCount", (IReportValueConverter)null);
            this.appendReportElement(reportContents, temp, "ProtectionStatus", "ProtectionStatus", (IReportValueConverter)null);
            this.appendNewLine(reportContents, "</SNCP>");
         }

         this.appendNewLine(reportContents, "</SNCPConfiguration>");
      }

   }

   private void getTrafficRoutingTable(StringBuffer reportContents) {
      MOID trafficRoutingMoid = new MOID("com.ericsson.mlne.im.fa.traffic_routing.CrossConnect", "CrossConnect", this.rootMoid.getResourceId());
      AxxMBTableMBean trafficRoutingTable = MMUUtil.getAxxMBTable(trafficRoutingMoid, "CrossConnectTable");
      if(null != trafficRoutingTable) {
         this.appendNewLine(reportContents, "<TrafficRoutingConfiguration>");
         MOID[] areaRows = trafficRoutingTable.getRows();

         for(int i = 0; i < areaRows.length; ++i) {
            MOID temp = areaRows[i];
            this.appendNewLine(reportContents, "<TR>");
            this.appendReportElement(reportContents, temp, "Interface1", "Interface1", new E1ReportGenerator.X1OverviewInterfaceConverter("InterfaceSideALabel"));
            this.appendReportElement(reportContents, temp, "Interface2", "Interface2", new E1ReportGenerator.X1OverviewInterfaceConverter("InterfaceSideALabel"));
            this.appendReportElement(reportContents, temp, "Name", "Name", (IReportValueConverter)null);
            this.appendNewLine(reportContents, "</TR>");
         }

         this.appendNewLine(reportContents, "</TrafficRoutingConfiguration>");
      }

   }

   private String getInputImpedance(MOID x1config) throws OperationException, MBeanException {
      String value = "-";
      Object inputImpedance = mgtServer.getAttribute(x1config, "InputImpedance");
      if(inputImpedance != null && !inputImpedance.toString().equals("") && inputImpedance instanceof AxxEnum) {
         value = ((AxxEnum)inputImpedance).getLabel();
      }

      return value;
   }

   private boolean isCN710(MOID m) {
      MOID cn710 = new MOID("com.ericsson.mlcn.im.MLCN", "eqroot", m.getResourceId());

      try {
         return ((Boolean)MgtUtil.getInstance().getAttribute(cn710, "IsCN710")).booleanValue();
      } catch (Exception var4) {
         return false;
      }
   }

   private boolean isCN810(MOID m) {
      MOID cn810 = new MOID("com.ericsson.mlcn.im.MLCN", "eqroot", m.getResourceId());

      try {
         return ((Boolean)MgtUtil.getInstance().getAttribute(cn810, "IsCN810")).booleanValue();
      } catch (Exception var4) {
         return false;
      }
   }

   private boolean isCN810_R2(MOID m) {
      MOID cn810 = new MOID("com.ericsson.mlcn.im.MLCN", "eqroot", m.getResourceId());

      try {
         return ((Boolean)MgtUtil.getInstance().getAttribute(cn810, "IsCN810_R2")).booleanValue();
      } catch (Exception var4) {
         return false;
      }
   }

   private class X1OverviewInterfaceConverter implements IReportValueConverter {

      private String convertingAttr;


      public Object convert(Object obj) {
         String interfaceSideLabel = "";
         if(obj instanceof GUIAttRef) {
            MOID moid = ((GUIAttRef)obj).getMoid();

            try {
               interfaceSideLabel = (String)MgtUtil.getInstance().getAttribute(moid, this.convertingAttr);
            } catch (Exception var5) {
               return null;
            }
         }

         return interfaceSideLabel;
      }

      public X1OverviewInterfaceConverter(String convertingAttr) {
         this.convertingAttr = convertingAttr;
      }
   }
}
