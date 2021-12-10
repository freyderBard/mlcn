package com.ericsson.mlcn.report;

import com.ericsson.mlcraft.report.AbstractReportGenerator;
import com.ericsson.mlcraft.report.IReportValueConverter;
import com.ericsson.mlcraft.report.AbstractReportGenerator.BooleanToEnableStatusConverter;
import com.ericsson.mlne.utils.MMUUtil;
import no.axxessit.client.util.MgtUtil;
import no.axxessit.mgt.AttRef;
import no.axxessit.mgt.AxxMBTableMBean;
import no.axxessit.mgt.MBeanException;
import no.axxessit.mgt.MBeanProxy;
import no.axxessit.mgt.MOID;
import no.axxessit.mgt.MOIDList;
import no.axxessit.mgt.OperationException;
import no.axxessit.mgt.TableRef;

public class EthernetReportGenerator extends AbstractReportGenerator {

   public EthernetReportGenerator(MOID rootMoid) {
      super(rootMoid);
   }

   public String getReport() {
      MOID neMoid = MgtUtil.getInstance().getNe(this.rootMoid);
      if(!MMUUtil.isCNX10(neMoid) && !MMUUtil.isCNX10Release2(neMoid)) {
         StringBuffer reportContents = new StringBuffer();
         this.appendNewLine(reportContents, "<EthernetConfiguration>");
         MOID tempMoid = new MOID("com.ericsson.mlne.im.fa.ethernet.EthLanWanRlimeEplStatusAndConfig", "dummy", this.rootMoid.getResourceId());
         MOIDList tempMoids = mgtServer.queryMBeans(tempMoid);
         MOID ethMoid = null;
         if(tempMoids.size() <= 0) {
            return null;
         } else {
            ethMoid = (MOID)tempMoids.get(0);
            MOID tempMoid2 = new MOID("com.ericsson.mlne.im.fa.ethernet.MLCNEplConfig", "dummy", this.rootMoid.getResourceId());
            MOIDList tempMoids2 = mgtServer.queryMBeans(tempMoid2);
            MOID eplMoid = null;
            if(tempMoids2.size() > 0) {
               eplMoid = (MOID)tempMoids2.get(0);
               MOID eplConfigureQoSMoid = null;

               try {
                  AttRef e = (AttRef)mgtServer.getAttribute(eplMoid, "EplConfigureQoS");
                  eplConfigureQoSMoid = e.getMoid();
               } catch (Exception var15) {
                  log.error(var15);
                  return null;
               }

               this.appendNewLine(reportContents, "<LAN>");
               this.appendReportElement(reportContents, ethMoid, "LanInterface.AdminStatus", "AdminStatus", (IReportValueConverter)null);
               this.appendReportElement(reportContents, ethMoid, "LanInterface.LanStatusAndConfig.AutoNegotiate", "AutoNegotiate", (IReportValueConverter)null);
               this.appendReportElement(reportContents, ethMoid, "LanInterface.LanStatusAndConfig.MdiMdix", "MdiMdix", (IReportValueConverter)null);
               this.appendReportElement(reportContents, ethMoid, "LanInterface.LanStatusAndConfig.FlowControl", "FlowControl", (IReportValueConverter)null);
               this.appendReportElement(reportContents, ethMoid, "LanInterface.LanStatusAndConfig.ManualConfigSpeed", "ManualConfigSpeed", (IReportValueConverter)null);
               this.appendNewLine(reportContents, "</LAN>");
               this.appendNewLine(reportContents, "<WAN>");
               this.appendReportElement(reportContents, ethMoid, "WanInterface.WanStatusAndConfig.HoldOffTime", "HoldOffTime", (IReportValueConverter)null);
               this.appendReportElement(reportContents, ethMoid, "WanInterface.WanStatusAndConfig.WaitToRestore", "WaitToRestore", (IReportValueConverter)null);
               this.appendReportElement(reportContents, ethMoid, "RLIme.RLImeStatusAndConfig.NoTrafficAlarmEnable", "NoTrafficAlarmEnable", (IReportValueConverter)null);
               this.appendReportElement(reportContents, ethMoid, "EplStatusandConfig.LinkLossForward", "LinkLossForward", new BooleanToEnableStatusConverter(this));
               this.appendNewLine(reportContents, "</WAN>");
               this.appendNewLine(reportContents, "<QoS>");
               this.appendNewLine(reportContents, "<UserPriorityMapping>");
               this.appendReportElement(reportContents, eplMoid, "EplConfigureQoS.DefaultUserPriority", "DefaultUserPriority", (IReportValueConverter)null);
               this.appendReportElement(reportContents, eplMoid, "EplConfigureQoS.TrustedPort", "TrustedPort", (IReportValueConverter)null);
               this.appendNewLine(reportContents, "</UserPriorityMapping>");
               this.appendNewLine(reportContents, "<NetworkPriorityMapping>");
               this.appendReportElement(reportContents, eplMoid, "EplConfigureQoS.PcpSelection", "PcpSelection", (IReportValueConverter)null);
               this.appendReportElement(reportContents, eplMoid, "EplConfigureQoS.MappingType", "MappingType", (IReportValueConverter)null);
               this.appendNewLine(reportContents, "</NetworkPriorityMapping>");
               this.appendNewLine(reportContents, "</QoS>");
               this.appendNewLine(reportContents, "<PriorityMapping>");
               this.appendNewLine(reportContents, "<UserPriorityMapping>");

               AxxMBTableMBean trafficClassTable;
               MOID[] trafficClassRows;
               int i;
               TableRef var25;
               try {
                  var25 = (TableRef)mgtServer.getAttribute(eplConfigureQoSMoid, "PriorityMapping");
                  trafficClassTable = (AxxMBTableMBean)MBeanProxy.createInstance(var25.getMoid(), mgtServer);
                  trafficClassRows = trafficClassTable.getRows();

                  for(i = 0; i < trafficClassRows.length; ++i) {
                     this.appendNewLine(reportContents, "<UserPriority>");
                     this.appendReportElement(reportContents, trafficClassRows[i], "UserPriority", (String)null, (IReportValueConverter)null);
                     this.appendReportElement(reportContents, trafficClassRows[i], "NetworkPriority", (String)null, (IReportValueConverter)null);
                     this.appendNewLine(reportContents, "</UserPriority>");
                  }
               } catch (OperationException var22) {
                  log.error(var22);
               } catch (MBeanException var23) {
                  log.error(var23);
               } catch (ClassNotFoundException var24) {
                  log.error(var24);
               }

               this.appendNewLine(reportContents, "</UserPriorityMapping>");
               this.appendNewLine(reportContents, "<PriorityTCMapping>");

               try {
                  var25 = (TableRef)mgtServer.getAttribute(eplConfigureQoSMoid, "TrafficClassMapping");
                  trafficClassTable = (AxxMBTableMBean)MBeanProxy.createInstance(var25.getMoid(), mgtServer);
                  trafficClassRows = trafficClassTable.getRows();

                  for(i = 0; i < trafficClassRows.length; ++i) {
                     this.appendNewLine(reportContents, "<TrafficClass>");
                     this.appendReportElement(reportContents, trafficClassRows[i], "NetworkPriority", (String)null, (IReportValueConverter)null);
                     this.appendReportElement(reportContents, trafficClassRows[i], "TrafficClass", (String)null, (IReportValueConverter)null);
                     this.appendNewLine(reportContents, "</TrafficClass>");
                  }
               } catch (OperationException var19) {
                  log.error(var19);
               } catch (MBeanException var20) {
                  log.error(var20);
               } catch (ClassNotFoundException var21) {
                  log.error(var21);
               }

               this.appendNewLine(reportContents, "</PriorityTCMapping>");
               this.appendNewLine(reportContents, "</PriorityMapping>");
               this.appendNewLine(reportContents, "<TrafficClassQueues>");

               try {
                  var25 = (TableRef)mgtServer.getAttribute(eplConfigureQoSMoid, "TrafficClass");
                  trafficClassTable = (AxxMBTableMBean)MBeanProxy.createInstance(var25.getMoid(), mgtServer);
                  trafficClassRows = trafficClassTable.getRows();

                  for(i = 0; i < trafficClassRows.length; ++i) {
                     this.appendNewLine(reportContents, "<TCQueue>");
                     this.appendReportElement(reportContents, trafficClassRows[i], "TrafficClass", (String)null, (IReportValueConverter)null);
                     this.appendReportElement(reportContents, trafficClassRows[i], "PacketAgingTime", (String)null, (IReportValueConverter)null);
                     Integer size = (Integer)mgtServer.getAttribute(trafficClassRows[i], "NrOf64kSegments");
                     this.appendReportElement(reportContents, "NrOf64kSegments", size + " x 64kB");
                     this.appendNewLine(reportContents, "</TCQueue>");
                  }
               } catch (OperationException var16) {
                  log.error(var16);
               } catch (MBeanException var17) {
                  log.error(var17);
               } catch (ClassNotFoundException var18) {
                  log.error(var18);
               }

               this.appendNewLine(reportContents, "</TrafficClassQueues>");
               this.appendNewLine(reportContents, "</EthernetConfiguration>");
               return reportContents.toString();
            } else {
               return null;
            }
         }
      } else {
         return "";
      }
   }
}
