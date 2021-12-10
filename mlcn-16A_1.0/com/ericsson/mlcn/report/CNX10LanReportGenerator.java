package com.ericsson.mlcn.report;

import com.ericsson.mlcraft.common.util.RelationSearchUtil;
import com.ericsson.mlcraft.report.AbstractReportGenerator;
import com.ericsson.mlne.utils.MMUUtil;
import java.util.ArrayList;
import java.util.Iterator;
import no.axxessit.client.util.MgtUtil;
import no.axxessit.common.val.AxxEnum;
import no.axxessit.mgt.MOID;
import no.axxessit.mgt.MOIDList;

public class CNX10LanReportGenerator extends AbstractReportGenerator {

   public CNX10LanReportGenerator(MOID rootMoid) {
      super(rootMoid);
   }

   public String getReport() {
      StringBuffer reportContents = new StringBuffer();
      this.appendNewLine(reportContents, "<CNX10LanConfiguration>");
      MOID ethernetMoid = RelationSearchUtil.getChild(this.rootMoid, "com.ericsson.mlne.im.fa.ethernet.Ethernet");
      MOIDList lans = ethernetMoid != null?RelationSearchUtil.getChildren(ethernetMoid, "com.ericsson.mlne.im.fa.ethernet.LanInterface"):null;
      MOIDList lanDcns = ethernetMoid != null?RelationSearchUtil.getChildren(ethernetMoid, "com.ericsson.mlne.im.fa.ethernet.LanDcnInterface"):null;
      ArrayList usedLans = new ArrayList();
      this.appendNewLine(reportContents, "<LANs>");
      Iterator i$;
      MOID usedLan;
      if(lans != null) {
         i$ = lans.iterator();

         while(i$.hasNext()) {
            usedLan = (MOID)i$.next();
            AxxEnum pullDownInterfaceUsage = (AxxEnum)MMUUtil.getAttribute(usedLan, "LanStatusAndConfig.PullDownInterfaceUsage");
            if(513 != pullDownInterfaceUsage.value()) {
               usedLans.add(usedLan);
            }

            this.appendNewLine(reportContents, "<LAN>");
            this.appendReportElement(reportContents, usedLan, "MocMoi", "MocMoi");
            this.appendReportElement(reportContents, usedLan, "AliasName", "AliasName");
            this.appendReportElement(reportContents, usedLan, "LanStatusAndConfig.PullDownInterfaceUsage", "PullDownInterfaceUsage");
            this.appendReportElement(reportContents, usedLan, "OperStatus", "OperStatus");
            this.appendReportElement(reportContents, usedLan, "LanStatusAndConfig.AdminStatus", "AdminStatus");
            this.appendReportElement(reportContents, usedLan, "Notifications", "Notifications");
            this.appendReportElement(reportContents, usedLan, "LinkOam.LinkAlarms.EthernetDown", "EthernetDown");
            this.appendReportElement(reportContents, usedLan, "LinkOam.LinkAlarms.EthernetSyncSupport", "EthernetSyncSupport");
            this.appendReportElement(reportContents, usedLan, "LanStatusAndConfig.MdiMdix", "MdiMdix");
            this.appendReportElement(reportContents, usedLan, "LanStatusAndConfig.ExtendedEgressQoS", "ExtendedEgressQoS");
            this.appendReportElement(reportContents, usedLan, "LanStatusAndConfig.AutoNegotiate", "AutoNegotiate");
            this.appendReportElement(reportContents, usedLan, "LanStatusAndConfig.FlowControl", "FlowControl");
            this.appendReportElement(reportContents, usedLan, "LanStatusAndConfig.EthernetSyncMode", "EthernetSyncMode");
            this.appendReportElement(reportContents, usedLan, "LanStatusAndConfig.ManualConfigSpeed", "ManualConfigSpeed");
            this.appendReportElement(reportContents, usedLan, "ALS.ALSMode", "ALSMode");
            this.appendNewLine(reportContents, "</LAN>");
         }
      }

      if(lanDcns != null) {
         i$ = lanDcns.iterator();

         while(i$.hasNext()) {
            usedLan = (MOID)i$.next();
            this.appendNewLine(reportContents, "<LAN>");
            this.appendReportElement(reportContents, usedLan, "MocMoi", "MocMoi");
            if(this.isLANDCNAliasNameExisted(usedLan)) {
               this.appendReportElement(reportContents, usedLan, "AliasName", "AliasName");
            } else {
               this.appendReportElement(reportContents, "AliasName", "-");
            }

            this.appendReportElement(reportContents, usedLan, "EthInterfaceStatusAndConfig.PullDownInterfaceUsage", "PullDownInterfaceUsage");
            this.appendReportElement(reportContents, usedLan, "OperStatus", "OperStatus");
            this.appendReportElement(reportContents, usedLan, "EthInterfaceStatusAndConfig.AdminStatus", "AdminStatus");
            this.appendReportElement(reportContents, usedLan, "Notifications", "Notifications");
            this.appendReportElement(reportContents, "EthernetDown", "-");
            this.appendReportElement(reportContents, "EthernetSyncSupport", "-");
            this.appendReportElement(reportContents, "MdiMdix", "-");
            this.appendReportElement(reportContents, "ExtendedEgressQoS", "-");
            this.appendReportElement(reportContents, "AutoNegotiate", "-");
            this.appendReportElement(reportContents, "FlowControl", "-");
            this.appendReportElement(reportContents, "EthernetSyncMode", "-");
            this.appendReportElement(reportContents, "ManualConfigSpeed", "-");
            this.appendReportElement(reportContents, "ALSMode", "-");
            this.appendNewLine(reportContents, "</LAN>");
         }
      }

      this.appendNewLine(reportContents, "</LANs>");
      this.appendNewLine(reportContents, "<LinkOAMs>");
      i$ = usedLans.iterator();

      while(i$.hasNext()) {
         usedLan = (MOID)i$.next();
         this.appendNewLine(reportContents, "<LinkOAM>");
         this.appendReportElement(reportContents, usedLan, "MocMoi", "MocMoi");
         this.appendReportElement(reportContents, usedLan, "LinkOam.AdminStatus", "AdminStatus");
         this.appendReportElement(reportContents, usedLan, "LinkOam.LinkOamSupport.Mode", "Mode");
         this.appendReportElement(reportContents, usedLan, "LinkOam.IgnoreRxLoopBackCmd", "IgnoreRxLoopBackCmd");
         this.appendReportElement(reportContents, usedLan, "LinkOam.LinkMonitoringConfig.ErrFrameWindow", "ErrFrameWindow");
         this.appendReportElement(reportContents, usedLan, "LinkOam.LinkMonitoringConfig.ErrFrameThreshold", "ErrFrameThreshold");
         this.appendReportElement(reportContents, usedLan, "LinkOam.LinkMonitoringConfig.ErrFramePeriodWindow", "ErrFramePeriodWindow");
         this.appendReportElement(reportContents, usedLan, "LinkOam.LinkMonitoringConfig.ErrFramePeriodThreshold", "ErrFramePeriodThreshold");
         this.appendReportElement(reportContents, usedLan, "LinkOam.LinkMonitoringConfig.ErrFrameSecondsSummaryWindow", "ErrFrameSecondsSummaryWindow");
         this.appendReportElement(reportContents, usedLan, "LinkOam.LinkMonitoringConfig.ErrFrameSecondsSummaryThreshold", "ErrFrameSecondsSummaryThreshold");
         this.appendReportElement(reportContents, usedLan, "LinkOam.LinkMonitoringConfig.ErrSymbolPeriodThreshold", "ErrSymbolPeriodThreshold");
         this.appendReportElement(reportContents, usedLan, "LinkOam.LinkAlarms.LinkFault", "LinkFault");
         this.appendReportElement(reportContents, usedLan, "LinkOam.LinkAlarms.LinkOamLoopBack", "LinkOamLoopBack");
         this.appendReportElement(reportContents, usedLan, "LinkOam.LinkEvents.CriticalEvent", "CriticalEvent");
         this.appendReportElement(reportContents, usedLan, "LinkOam.LinkEvents.DyingGasp", "DyingGasp");
         this.appendReportElement(reportContents, usedLan, "LinkOam.LinkEvents.ErroredFrame", "ErroredFrame");
         this.appendReportElement(reportContents, usedLan, "LinkOam.LinkEvents.ErroredFramePeriod", "ErroredFramePeriod");
         this.appendReportElement(reportContents, usedLan, "LinkOam.LinkEvents.ErroredSymbolPeriod", "ErroredSymbolPeriod");
         this.appendReportElement(reportContents, usedLan, "LinkOam.LinkEvents.ErroredFrameSecondsSummary", "ErroredFrameSecondsSummary");
         this.appendNewLine(reportContents, "</LinkOAM>");
      }

      this.appendNewLine(reportContents, "</LinkOAMs>");
      this.appendNewLine(reportContents, "</CNX10LanConfiguration>");
      return reportContents.toString();
   }

   private boolean isLANDCNAliasNameExisted(MOID lanDcnMoid) {
      MOID neMoid = MgtUtil.getInstance().getNe(lanDcnMoid);
      boolean isCN710 = false;
      boolean isCN810 = false;
      boolean isCN810R2 = false;
      isCN710 = MMUUtil.isCN710(neMoid);
      isCN810 = MMUUtil.isCN810(neMoid);
      isCN810R2 = MMUUtil.isCN810_R2(neMoid);
      return !isCN710 && !isCN810 && isCN810R2;
   }
}
