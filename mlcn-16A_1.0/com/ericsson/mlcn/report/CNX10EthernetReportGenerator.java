package com.ericsson.mlcn.report;

import com.ericsson.mlcraft.common.util.BitsWrap;
import com.ericsson.mlcraft.common.util.RelationSearchUtil;
import com.ericsson.mlcraft.report.AbstractReportGenerator;
import com.ericsson.mlcraft.report.IReportValueConverter;
import com.ericsson.mlcraft.report.AbstractReportGenerator.BooleanToEnableStatusConverter;
import com.ericsson.mlcraft.report.AbstractReportGenerator.BooleanToYesNoConverter;
import com.ericsson.mlne.utils.MMUUtil;
import java.util.ArrayList;
import java.util.HashMap;
import no.axxessit.client.util.MgtUtil;
import no.axxessit.common.val.AxxEnum;
import no.axxessit.common.val.ByteArray;
import no.axxessit.mgt.AttRef;
import no.axxessit.mgt.AxxMBTableMBean;
import no.axxessit.mgt.MBeanException;
import no.axxessit.mgt.MBeanProxy;
import no.axxessit.mgt.MOID;
import no.axxessit.mgt.MOIDList;
import no.axxessit.mgt.OperationException;
import no.axxessit.mgt.TableRef;

public class CNX10EthernetReportGenerator extends AbstractReportGenerator {

   public CNX10EthernetReportGenerator(MOID rootMoid) {
      super(rootMoid);
   }

   public String getReport() {
      MOID neMoid = MgtUtil.getInstance().getNe(this.rootMoid);
      if(!MMUUtil.isCNX10(neMoid) && !MMUUtil.isCNX10Release2(neMoid)) {
         return "";
      } else {
         StringBuffer reportContents = new StringBuffer();
         this.appendNewLine(reportContents, "<CNX10EthernetConfiguration>");
         MOID ethernetMoid = RelationSearchUtil.getChild(this.rootMoid, "com.ericsson.mlne.im.fa.ethernet.Ethernet");
         MOID ethernetSwitchMoid = RelationSearchUtil.getChild(ethernetMoid, "com.ericsson.mlne.im.fa.ethernet_bridge.EthernetSwitch");
         this.appendNewLine(reportContents, "<Basic>");
         this.appendReportElement(reportContents, ethernetSwitchMoid, "BridgeConfigAndStatus.BridgeMode", "SwitchMode");
         this.appendReportElement(reportContents, ethernetSwitchMoid, "BridgeConfigAndStatus.AgingTime", "AgingTime");
         String numOfTrQueues = "-";

         try {
            numOfTrQueues = (String)mgtServer.getAttribute(ethernetMoid, "NumOfTrQueues");
         } catch (Exception var44) {
            ;
         }

         this.appendReportElement(reportContents, "NumberOfTrafficClasses", numOfTrQueues);
         this.appendNewLine(reportContents, "</Basic>");
         this.appendNewLine(reportContents, "<QoS>");
         this.appendReportElement(reportContents, ethernetSwitchMoid, "BridgeConfigAndStatus.NetworkPcpSelection", "PCPSelection");
         this.appendReportElement(reportContents, ethernetSwitchMoid, "BridgeConfigAndStatus.PriorityMappingType", "Prioritymapping");
         this.appendReportElement(reportContents, ethernetSwitchMoid, "BridgeConfigAndStatus.SchedulerProfiles", "SchedulerProfiles");
         this.appendNewLine(reportContents, "<TrafficClassQueueTable>");
         Object scQoSObj = MMUUtil.getAttribute(ethernetSwitchMoid, "SwitchConfigureQoS");
         int bandwidthProfileTable;
         MOID searchMoid;
         if(scQoSObj != null && scQoSObj instanceof AttRef) {
            Object uniPorts = MMUUtil.getAttribute(((AttRef)scQoSObj).getMoid(), "SwitchQueuesTable");
            if(uniPorts != null && uniPorts instanceof TableRef) {
               TableRef switchPortMoidSearch = (TableRef)uniPorts;

               try {
                  AxxMBTableMBean ports = (AxxMBTableMBean)MBeanProxy.createInstance(switchPortMoidSearch.getMoid(), mgtServer);
                  MOID[] switchPortSize = ports.getRows();

                  for(bandwidthProfileTable = 0; bandwidthProfileTable < switchPortSize.length; ++bandwidthProfileTable) {
                     searchMoid = switchPortSize[bandwidthProfileTable];
                     this.appendNewLine(reportContents, "<TrafficClassQueue>");
                     String lagMoids = (String)MMUUtil.getAttribute(searchMoid, "NetworkPriority");
                     this.appendReportElement(reportContents, searchMoid, "TC", "TC");
                     this.appendReportElement(reportContents, searchMoid, "NetworkPriority", "NetworkPriority");
                     this.appendReportElement(reportContents, "Valid", lagMoids != null && !"".equals(lagMoids) && !"N/A".equals(lagMoids)?"Yes":"No");
                     this.appendReportElement(reportContents, searchMoid, "Aging", "Aging");
                     this.appendReportElement(reportContents, searchMoid, "WredEnable", "WredEnable", new BooleanToYesNoConverter(this));
                     this.appendReportElement(reportContents, searchMoid, "SchedulerType", "SchedulerType");
                     this.appendReportElement4QueuesTable(reportContents, searchMoid, "WfqWeight", "WfqWeight");
                     this.appendReportElement4QueuesTable(reportContents, searchMoid, "ColorDropEnable", "ColorDropEnable", new BooleanToEnableStatusConverter(this));
                     this.appendReportElement4QueuesTable(reportContents, searchMoid, "YellowLimit", "YellowLimit");
                     this.appendNewLine(reportContents, "</TrafficClassQueue>");
                  }
               } catch (ClassNotFoundException var45) {
                  log.error(var45);
               }
            }
         }

         this.appendNewLine(reportContents, "</TrafficClassQueueTable>");
         this.appendNewLine(reportContents, "</QoS>");
         MOIDList var46 = new MOIDList();
         this.appendNewLine(reportContents, "<SwitchPorts>");
         MOID var47 = new MOID("com.ericsson.mlne.im.fa.ethernet_bridge.EthernetSwitchPort", "Switch Port:*", this.rootMoid.getResourceId());
         MOIDList var48 = mgtServer.queryMBeans(var47);
         int var49 = var48.size();

         Object spanningTreeConfigAndStatusObj;
         MOID var69;
         for(bandwidthProfileTable = 0; bandwidthProfileTable < var48.size(); ++bandwidthProfileTable) {
            searchMoid = (MOID)var48.get(bandwidthProfileTable);
            boolean var54 = false;
            boolean bridgeConfigAndStatusObj = false;
            spanningTreeConfigAndStatusObj = MMUUtil.getAttribute(searchMoid, "SwitchPortsConfigAndStatus.PortRole");
            if(spanningTreeConfigAndStatusObj != null && spanningTreeConfigAndStatusObj instanceof AxxEnum) {
               AxxEnum unicastFilterTableMBean = (AxxEnum)spanningTreeConfigAndStatusObj;
               int multicastFilterTableMBean = unicastFilterTableMBean.value();
               if(multicastFilterTableMBean != 1 && multicastFilterTableMBean != 4 && multicastFilterTableMBean != 5) {
                  if(multicastFilterTableMBean == 3) {
                     var54 = true;
                  }
               } else {
                  bridgeConfigAndStatusObj = true;
                  var46.add(searchMoid);
               }
            }

            this.appendNewLine(reportContents, "<SwitchPort>");
            this.appendReportElement(reportContents, searchMoid, "SwitchPortsConfigAndStatus.PortID", "PortID");
            this.appendReportElement(reportContents, searchMoid, "SwitchPortsConfigAndStatus.PortRole", "PortRole");
            this.appendReportElement(reportContents, searchMoid, "SwitchPortsConfigAndStatus.ConnectedInterfaceName", "ConnectedInterfaceName");
            this.appendReportElement(reportContents, searchMoid, "SwitchPortsConfigAndStatus.LagUsage", "LagUsage");
            this.appendReportElement(reportContents, searchMoid, "SwitchPortsConfigAndStatus.LagName", "LagName");
            if(bridgeConfigAndStatusObj) {
               this.appendReportElement(reportContents, searchMoid, "SwitchPortsConfigAndStatus.SwitchPortConfig_UNI.AcceptUntaggedFrames", "AcceptUntaggedFrames");
               this.appendReportElement(reportContents, searchMoid, "SwitchPortsConfigAndStatus.SwitchPortConfig_UNI.AcceptPriorityFrames", "AcceptPriorityFrames");
               this.appendReportElement(reportContents, searchMoid, "SwitchPortsConfigAndStatus.SwitchPortConfig_UNI.AcceptVlanTaggedFrames", "AcceptVlanTaggedFrames");
               this.appendReportElement(reportContents, searchMoid, "SwitchPortsConfigAndStatus.SwitchPortConfig_UNI.PVidForUntaggedFrames", "PVidForUntaggedFrames");
               this.appendReportElement(reportContents, searchMoid, "SwitchPortsConfigAndStatus.SwitchPortConfig_UNI.MaxFrameSizeConfig", "MaxFrameSizeConfig");
               this.appendReportElement(reportContents, searchMoid, "SwitchPortsConfigAndStatus.SwitchPortConfig_UNI.SwitchPortSecurity.ForbiddenEgressPorts", "ForbiddenEgressPorts", new CNX10EthernetReportGenerator.ForbiddenEgressPortsConverter(var49));
               this.appendReportElement(reportContents, searchMoid, "SwitchPortsConfigAndStatus.SwitchPortConfig_UNI.SourceAddressWhiteList", "SourceAddressWhiteList");
               this.appendReportElement(reportContents, searchMoid, "SwitchPortsConfigAndStatus.SwitchPortConfig_UNI.SwitchPortSecurity.MaxNumOfSupportedMacAddresses", "MaxNumOfSupportedMacAddresses");
               this.appendReportElement(reportContents, searchMoid, "SwitchPortsConfigAndStatus.SwitchPortConfig_UNI.StormProtectionMacDestinationLookupFailure.DlfStormProtectionStatus", "DlfStormProtectionStatus");
               this.appendReportElement(reportContents, searchMoid, "SwitchPortsConfigAndStatus.SwitchPortConfig_UNI.StormProtectionMacDestinationLookupFailure.DlfMaxBandwidth", "DlfMaxBandwidth");
               this.appendReportElement(reportContents, searchMoid, "SwitchPortsConfigAndStatus.SwitchPortConfig_UNI.StormProtectionMulticast.MulticastStormProtectionStatus", "MulticastStormProtectionStatus");
               this.appendReportElement(reportContents, searchMoid, "SwitchPortsConfigAndStatus.SwitchPortConfig_UNI.StormProtectionMulticast.MulticastMaxBandwidth", "MulticastMaxBandwidth");
               this.appendReportElement(reportContents, searchMoid, "SwitchPortsConfigAndStatus.StormProtectionBroadcast.BroadcastStormProtectionStatus", "BroadcastStormProtectionStatus");
               this.appendReportElement(reportContents, searchMoid, "SwitchPortsConfigAndStatus.StormProtectionBroadcast.BroadcastMaxBandwidth", "BroadcastMaxBandwidth");
            } else {
               this.appendReportElement(reportContents, "AcceptUntaggedFrames", "-");
               this.appendReportElement(reportContents, "AcceptPriorityFrames", "-");
               this.appendReportElement(reportContents, "AcceptVlanTaggedFrames", "-");
               this.appendReportElement(reportContents, "PVidForUntaggedFrames", "-");
               this.appendReportElement(reportContents, "MaxFrameSizeConfig", "-");
               this.appendReportElement(reportContents, "ForbiddenEgressPorts", "-");
               this.appendReportElement(reportContents, "SourceAddressWhiteList", "-");
               this.appendReportElement(reportContents, "MaxNumOfSupportedMacAddresses", "-");
               this.appendReportElement(reportContents, "DlfStormProtectionStatus", "-");
               this.appendReportElement(reportContents, "DlfMaxBandwidth", "-");
               this.appendReportElement(reportContents, "MulticastStormProtectionStatus", "-");
               this.appendReportElement(reportContents, "MulticastMaxBandwidth", "-");
               this.appendReportElement(reportContents, "BroadcastStormProtectionStatus", "-");
               this.appendReportElement(reportContents, "BroadcastMaxBandwidth", "-");
            }

            Object var64 = MMUUtil.getAttribute(searchMoid, "SwitchPortsConfigAndStatus.SwitchPortConfig_MirrorPort");
            if(var54) {
               var69 = ((AttRef)var64).getMoid();
               this.appendReportElement(reportContents, var69, "MirrorFromPort", "MirrorFromPort");
               this.appendReportElement(reportContents, var69, "EgressCvidOnMirroredPort", "EgressCvidOnMirroredPort");
               this.appendReportElement(reportContents, var69, "PortMirroringMode", "PortMirroringMode");
            } else {
               this.appendReportElement(reportContents, "MirrorFromPort", "-");
               this.appendReportElement(reportContents, "EgressCvidOnMirroredPort", "-");
               this.appendReportElement(reportContents, "PortMirroringMode", "-");
            }

            this.appendNewLine(reportContents, "</SwitchPort>");
         }

         this.appendNewLine(reportContents, "</SwitchPorts>");
         this.appendNewLine(reportContents, "<SwitchPortsQoS>");
         int eplSearchMoid;
         int lanIndices;
         MOID wanIndices;
         int var58;
         MOID var65;
         if(var46.size() > 0) {
            MOID var50 = (MOID)var46.get(0);
            HashMap var51 = new HashMap();
            AxxMBTableMBean var55 = MMUUtil.getAxxMBTable(var50, "SwitchPortConfigureQoS.SwitchPortPolicingTable");
            if(var55 != null) {
               MOID[] var57 = var55.getRows();

               for(var58 = 0; var58 < var57.length; ++var58) {
                  var65 = var57[var58];
                  String var71 = var65.getObjectId().getId().split(":")[1];
                  var51.put(var71, var65);
               }
            }

            AxxMBTableMBean var59 = MMUUtil.getAxxMBTable(var50, "SwitchPortConfigureQoS.SwitchPortUserPriorityConfigTable");
            MOID[] var62 = var59.getRows();

            for(int var67 = 0; var67 < var62.length; ++var67) {
               var69 = var62[var67];
               this.appendNewLine(reportContents, "<QoS>");
               this.appendReportElement(reportContents, var69, "PortID", "PortID");
               this.appendReportElement(reportContents, var69, "PortRole", "PortRole");
               this.appendReportElement(reportContents, var69, "TrustedPort", "TrustedPort");
               this.appendReportElement(reportContents, var69, "DefaultUserPriority", "DefaultUserPriority");
               this.appendReportElement(reportContents, var69, "BandwidthProfileTarget", "BandwidthProfileTarget");
               String sourceAddressWhitelistTableMBean = var69.getObjectId().getId().split(":")[1];
               if(var51.containsKey(sourceAddressWhitelistTableMBean)) {
                  MOID currentVLANsTableMBean = (MOID)var51.get(sourceAddressWhitelistTableMBean);
                  this.appendReportElement(reportContents, currentVLANsTableMBean, "SelectedBandwidthProfile", "SelectedBandwidthProfile");
                  this.appendReportElement(reportContents, currentVLANsTableMBean, "PolicingDefaultUserPriority", "PolicingDefaultUserPriority");
                  AxxMBTableMBean wan = MMUUtil.getAxxMBTable(currentVLANsTableMBean, "UserPriorityGroupConfig.UserPriorityGroupTable");
                  eplSearchMoid = wan != null?wan.getRows().length:0;
                  this.appendReportElement(reportContents, "UserPriorityGroupsSize", String.valueOf(eplSearchMoid));
                  this.appendNewLine(reportContents, "<UserPriorityGroups size=\"" + eplSearchMoid + "\">");
                  if(wan != null) {
                     MOID[] eplSearchResult = wan.getRows();

                     for(lanIndices = 0; lanIndices < eplSearchResult.length; ++lanIndices) {
                        this.appendNewLine(reportContents, "<UserPriorityGroup RowId=\"" + lanIndices + "\">");
                        wanIndices = eplSearchResult[lanIndices];
                        this.appendReportElement(reportContents, "RowId", String.valueOf(lanIndices));
                        this.appendReportElement(reportContents, wanIndices, "UserPriorityGroupGUI", "UserPriorityGroupGUI");
                        this.appendReportElement(reportContents, wanIndices, "UserPriority", "UserPriority");
                        this.appendReportElement(reportContents, wanIndices, "BandwidthProfile", "BandwidthProfile");
                        this.appendNewLine(reportContents, "</UserPriorityGroup>");
                     }
                  }

                  this.appendNewLine(reportContents, "</UserPriorityGroups>");
               }

               this.appendNewLine(reportContents, "</QoS>");
            }
         }

         this.appendNewLine(reportContents, "</SwitchPortsQoS>");
         this.appendNewLine(reportContents, "<BandwidthProfiles>");
         AxxMBTableMBean var52 = MMUUtil.getAxxMBTable(ethernetSwitchMoid, "BandwidthProfileTable");
         MOID var61;
         if(var52 != null) {
            MOID[] var53 = var52.getRows();

            for(int var56 = 0; var56 < var53.length; ++var56) {
               var61 = var53[var56];
               this.appendNewLine(reportContents, "<BandwidthProfile>");
               this.appendReportElement(reportContents, var61, "Name", "Name");
               this.appendReportElement(reportContents, var61, "CommittedBurstSize", "CommittedBurstSize");
               this.appendReportElement(reportContents, var61, "CommittedInformationRate", "CommittedInformationRate");
               this.appendReportElement(reportContents, var61, "ExcessBurstSize", "ExcessBurstSize");
               this.appendReportElement(reportContents, var61, "ExcessInformationRate", "ExcessInformationRate");
               this.appendReportElement(reportContents, var61, "CouplingFlag", "CouplingFlag");
               this.appendReportElement(reportContents, var61, "ColorMode", "ColorMode");
               this.appendReportElement(reportContents, var61, "CurrentUsers", "CurrentUsers");
               this.appendNewLine(reportContents, "</BandwidthProfile>");
            }
         }

         this.appendNewLine(reportContents, "</BandwidthProfiles>");
         searchMoid = new MOID("com.ericsson.mlne.im.fa.ethernet_bridge.LAG", "LAG:*", this.rootMoid.getResourceId());
         MOIDList var60 = mgtServer.queryMBeans(searchMoid);
         this.appendNewLine(reportContents, "<LinkAggregationGroups>");
         if(var60 != null && var60.size() > 0) {
            int var63 = var60.size();

            for(var58 = 0; var58 < var63; ++var58) {
               var65 = (MOID)var60.get(var58);
               this.appendNewLine(reportContents, "<LinkAggregationGroup>");
               this.appendReportElement(reportContents, var65, "Label", "Label");
               this.appendReportElement(reportContents, var65, "LAGConfigAndStatus.MasterPort", "MasterPort");
               this.appendReportElement(reportContents, var65, "LAGConfigAndStatus.ListOfMemberPorts", "ListOfMemberPorts");
               this.appendReportElement(reportContents, var65, "LAGConfigAndStatus.Name", "Name");
               this.appendReportElement(reportContents, var65, "LAGConfigAndStatus.NoTrafficAlarmEnable", "NoTrafficAlarmEnable");
               this.appendReportElement(reportContents, var65, "LAGConfigAndStatus.DegradedServiceAlarmEnable", "DegradedServiceAlarmEnable");
               this.appendReportElement(reportContents, var65, "LAGConfigAndStatus.DesignatedPort", "DesignatedPort");
               this.appendNewLine(reportContents, "</LinkAggregationGroup>");
            }
         }

         this.appendNewLine(reportContents, "</LinkAggregationGroups>");
         this.appendNewLine(reportContents, "<PriorityMappings>");
         int eplMoid;
         MOID[] eplSize;
         int i;
         int lanIdx;
         MOID[] var73;
         int var79;
         MOID var81;
         String var84;
         int var88;
         if(var46.size() > 0) {
            var61 = (MOID)var46.get(0);
            AxxMBTableMBean var68 = MMUUtil.getAxxMBTable(var61, "SwitchPortConfigureQoS.SwitchPortUserPriorityConfigTable");
            if(var68 != null) {
               MOID[] var72 = var68.getRows();
               var73 = var72;
               int var74 = var72.length;

               for(var79 = 0; var79 < var74; ++var79) {
                  var81 = var73[var79];
                  var84 = var81.getObjectId().getId();
                  var88 = var84.lastIndexOf(":");
                  String var89 = var84.substring(var88 + 1);
                  this.appendNewLine(reportContents, "<PriorityMapping Port=\"" + var89 + "\">");
                  Object var93 = MMUUtil.getAttribute(var81, "TrustedUserPriorityConfig");
                  if(var93 != null && var93 instanceof AttRef) {
                     MOID lanMocMois = ((AttRef)var93).getMoid();
                     AxxMBTableMBean wanMocMois = MMUUtil.getAxxMBTable(lanMocMois, "PriorityMappingTable");
                     if(wanMocMois != null) {
                        eplMoid = 0;
                        MOID[] eplQoSTableMBean = wanMocMois.getRows();
                        eplSize = eplQoSTableMBean;
                        i = eplQoSTableMBean.length;

                        for(lanIdx = 0; lanIdx < i; ++lanIdx) {
                           MOID wanIdx = eplSize[lanIdx];
                           if(eplMoid % 8 == 0) {
                              this.appendNewLine(reportContents, "<Row>");
                           }

                           this.appendNewLine(reportContents, "<Pair>");
                           this.appendReportElement(reportContents, wanIdx, "UserPriority", "UserPriority");
                           this.appendReportElement(reportContents, wanIdx, "NetworkPriority", "NetworkPriority");
                           this.appendNewLine(reportContents, "</Pair>");
                           if((eplMoid + 1) % 8 == 0) {
                              this.appendNewLine(reportContents, "</Row>");
                           }

                           ++eplMoid;
                        }
                     }
                  }

                  this.appendNewLine(reportContents, "</PriorityMapping>");
               }
            }
         }

         this.appendNewLine(reportContents, "</PriorityMappings>");
         this.appendNewLine(reportContents, "<UserDefinedNetworkPriorityMappings>");
         Object var66 = MMUUtil.getAttribute(ethernetSwitchMoid, "BridgeConfigAndStatus");
         AxxMBTableMBean var75;
         MOID[] var76;
         int var83;
         MOID var86;
         if(var66 != null && var66 instanceof AttRef) {
            AttRef var70 = (AttRef)var66;
            var75 = MMUUtil.getAxxMBTable(var70.getMoid(), "PriorityToTCMappingTable");
            if(var75 != null) {
               var73 = var75.getRows();
               var76 = var73;
               var79 = var73.length;

               for(var83 = 0; var83 < var79; ++var83) {
                  var86 = var76[var83];
                  this.appendNewLine(reportContents, "<TCMapping>");
                  this.appendReportElement(reportContents, var86, "NetworkPriority", "NetworkPriority");
                  this.appendReportElement(reportContents, var86, "TrafficClass", "TrafficClass");
                  this.appendNewLine(reportContents, "</TCMapping>");
               }
            }
         }

         this.appendNewLine(reportContents, "</UserDefinedNetworkPriorityMappings>");
         this.appendNewLine(reportContents, "<SpanningTree>");
         spanningTreeConfigAndStatusObj = MMUUtil.getAttribute(ethernetSwitchMoid, "BridgeConfigAndStatus.SpanningTreeConfigAndStatus");
         AxxMBTableMBean var78;
         MOID[] var82;
         MOID[] var85;
         MOID var90;
         if(spanningTreeConfigAndStatusObj != null && spanningTreeConfigAndStatusObj instanceof AttRef) {
            var65 = ((AttRef)spanningTreeConfigAndStatusObj).getMoid();
            this.appendReportElement(reportContents, var65, "ProtocolEnable", "ProtocolEnable");
            this.appendReportElement(reportContents, var65, "ProtocolType", "ProtocolType");
            AxxEnum var77 = (AxxEnum)MMUUtil.getAttribute(var65, "ProtocolType");
            if(var77.value() == 1) {
               this.appendReportElement(reportContents, var65, "RapidSpanningTreeBridgeConfigAndStatus.ForceVersion", "ForceVersion");
               this.appendReportElement(reportContents, var65, "RapidSpanningTreeBridgeConfigAndStatus.BridgeForwardDelay", "BridgeForwardDelay");
               this.appendReportElement(reportContents, var65, "RapidSpanningTreeBridgeConfigAndStatus.BridgeHelloTime", "BridgeHelloTime");
               this.appendReportElement(reportContents, var65, "RapidSpanningTreeBridgeConfigAndStatus.BridgeMaxAge", "BridgeMaxAge");
               this.appendReportElement(reportContents, var65, "RapidSpanningTreeBridgeConfigAndStatus.BridgePriority", "BridgePriority");
               this.appendReportElement(reportContents, var65, "RapidSpanningTreeBridgeConfigAndStatus.TxHoldCount", "TxHoldCount");
               this.appendNewLine(reportContents, "<Ports>");
               var78 = MMUUtil.getAxxMBTable(var65, "RapidSpanningTreeBridgeConfigAndStatus.RapidSpanningTreePortTable");
               if(var78 != null) {
                  var82 = var78.getRows();
                  var85 = var82;
                  eplSearchMoid = var82.length;

                  for(var88 = 0; var88 < eplSearchMoid; ++var88) {
                     var90 = var85[var88];
                     this.appendNewLine(reportContents, "<Port>");
                     this.appendReportElement(reportContents, var90, "PortIdentifier", "PortIdentifier");
                     this.appendReportElement(reportContents, var90, "PortPriority", "PortPriority");
                     this.appendReportElement(reportContents, var90, "AdministrativeBridgePortState", "AdministrativeBridgePortState");
                     this.appendReportElement(reportContents, var90, "AutoEdgePort", "AutoEdgePort");
                     this.appendReportElement(reportContents, var90, "PortAdminEdgePort", "PortAdminEdgePort");
                     this.appendReportElement(reportContents, var90, "ProtocolMigration", "ProtocolMigration");
                     this.appendReportElement(reportContents, var90, "PortAdminPointToPoint", "PortAdminPointToPoint");
                     this.appendReportElement(reportContents, var90, "OperPortPathCost", "OperPortPathCost");
                     this.appendReportElement(reportContents, var90, "AdminPortPathCost", "AdminPortPathCost");
                     this.appendReportElement(reportContents, var90, "PortMacEnabled", "PortMacEnabled");
                     this.appendNewLine(reportContents, "</Port>");
                  }
               }

               this.appendNewLine(reportContents, "</Ports>");
            } else if(var77.value() == 2) {
               this.appendReportElement(reportContents, var65, "MultipleSpanningTreeBridgeConfigAndStatus.ForceVersion", "ForceVersion");
               this.appendReportElement(reportContents, var65, "MultipleSpanningTreeBridgeConfigAndStatus.BridgeForwardDelay", "BridgeForwardDelay");
               this.appendReportElement(reportContents, var65, "MultipleSpanningTreeBridgeConfigAndStatus.BridgeMaxAge", "BridgeMaxAge");
               this.appendReportElement(reportContents, var65, "MultipleSpanningTreeBridgeConfigAndStatus.BridgePriority", "BridgePriority");
               this.appendReportElement(reportContents, var65, "MultipleSpanningTreeBridgeConfigAndStatus.TxHoldCount", "TxHoldCount");
               this.appendReportElement(reportContents, var65, "MultipleSpanningTreeBridgeConfigAndStatus.MaxHops", "MaxHops");
               this.appendReportElement(reportContents, var65, "MultipleSpanningTreeBridgeConfigAndStatus.FormatSelector", "FormatSelector");
               this.appendReportElement(reportContents, var65, "MultipleSpanningTreeBridgeConfigAndStatus.ConfigurationName", "ConfigurationName");
               this.appendReportElement(reportContents, var65, "MultipleSpanningTreeBridgeConfigAndStatus.RevisionLevel", "RevisionLevel");
               this.appendNewLine(reportContents, "<Ports>");
               var78 = MMUUtil.getAxxMBTable(var65, "MultipleSpanningTreeBridgeConfigAndStatus.MstCistPortTable");
               if(var78 != null) {
                  var82 = var78.getRows();
                  var85 = var82;
                  eplSearchMoid = var82.length;

                  for(var88 = 0; var88 < eplSearchMoid; ++var88) {
                     var90 = var85[var88];
                     this.appendNewLine(reportContents, "<Port>");
                     this.appendReportElement(reportContents, var90, "PortIdentifier", "PortIdentifier");
                     this.appendReportElement(reportContents, var90, "PortPriority", "PortPriority");
                     this.appendReportElement(reportContents, var90, "AdministrativeBridgePortState", "AdministrativeBridgePortState");
                     this.appendReportElement(reportContents, var90, "AutoEdgePort", "AutoEdgePort");
                     this.appendReportElement(reportContents, var90, "PortAdminEdgePort", "PortAdminEdgePort");
                     this.appendReportElement(reportContents, var90, "ProtocolMigration", "ProtocolMigration");
                     this.appendReportElement(reportContents, var90, "PortAdminPointToPoint", "PortAdminPointToPoint");
                     this.appendReportElement(reportContents, var90, "OperPortPathCost", "OperPortPathCost");
                     this.appendReportElement(reportContents, var90, "AdminPortPathCost", "AdminPortPathCost");
                     this.appendReportElement(reportContents, var90, "PortPathCost", "PortPathCost");
                     this.appendReportElement(reportContents, var90, "PortRestrictedRole", "PortRestrictedRole");
                     this.appendReportElement(reportContents, var90, "PortRestrictedTcn", "PortRestrictedTcn");
                     this.appendReportElement(reportContents, var90, "PortMacEnabled", "PortMacEnabled");
                     this.appendNewLine(reportContents, "</Port>");
                  }
               }

               this.appendNewLine(reportContents, "</Ports>");
            }
         }

         this.appendNewLine(reportContents, "</SpanningTree>");
         this.appendNewLine(reportContents, "<StaticUnicastFilters>");
         var75 = MMUUtil.getAxxMBTable(ethernetSwitchMoid, "BridgeConfigAndStatus.VlanConfigAndStatus.StaticUnicastForwardingTable");
         if(var75 != null) {
            var73 = var75.getRows();
            var76 = var73;
            var79 = var73.length;

            for(var83 = 0; var83 < var79; ++var83) {
               var86 = var76[var83];
               this.appendNewLine(reportContents, "<Filter>");
               this.appendReportElement(reportContents, var86, "VlanId", "VlanId");
               this.appendReportElement(reportContents, var86, "MacAddress", "MacAddress");
               this.appendReportElement(reportContents, var86, "ReceivePort", "ReceivePort");
               this.appendReportElement(reportContents, var86, "AllowedToGoToEgressPort", "AllowedToGoToEgressPort");
               this.appendReportElement(reportContents, var86, "StaticStatus", "StaticStatus");
               this.appendNewLine(reportContents, "</Filter>");
            }
         }

         this.appendNewLine(reportContents, "</StaticUnicastFilters>");
         this.appendNewLine(reportContents, "<StaticMulticastFilters>");
         AxxMBTableMBean var80 = MMUUtil.getAxxMBTable(ethernetSwitchMoid, "BridgeConfigAndStatus.VlanConfigAndStatus.StaticMulticastForwardingTable");
         if(var80 != null) {
            var76 = var80.getRows();
            var82 = var76;
            var83 = var76.length;

            for(eplSearchMoid = 0; eplSearchMoid < var83; ++eplSearchMoid) {
               MOID var91 = var82[eplSearchMoid];
               this.appendNewLine(reportContents, "<Filter>");
               this.appendReportElement(reportContents, var91, "VlanId", "VlanId");
               this.appendReportElement(reportContents, var91, "MacAddress", "MacAddress");
               this.appendReportElement(reportContents, var91, "EgressPorts", "EgressPorts");
               this.appendReportElement(reportContents, var91, "StaticStatus", "StaticStatus");
               this.appendNewLine(reportContents, "</Filter>");
            }
         }

         this.appendNewLine(reportContents, "</StaticMulticastFilters>");
         this.appendNewLine(reportContents, "<MACWhiteLists>");
         var78 = MMUUtil.getAxxMBTable(ethernetSwitchMoid, "BridgeConfigAndStatus.SourceAddressWhitelistTable");
         if(var78 != null) {
            var82 = var78.getRows();
            var85 = var82;
            eplSearchMoid = var82.length;

            for(var88 = 0; var88 < eplSearchMoid; ++var88) {
               var90 = var85[var88];
               this.appendNewLine(reportContents, "<List>");
               this.appendReportElement(reportContents, var90, "Name", "Name");
               this.appendNewLine(reportContents, "<MacAddresses>");
               AxxMBTableMBean var94 = MMUUtil.getAxxMBTable(var90, "SourceAddressWhitelist");
               if(var94 != null) {
                  MOID[] var95 = var94.getRows();
                  MOID[] var106 = var95;
                  eplMoid = var95.length;

                  for(int var111 = 0; var111 < eplMoid; ++var111) {
                     MOID var113 = var106[var111];
                     this.appendReportElement(reportContents, var113, "MacAddress", "MacAddress");
                  }
               }

               this.appendNewLine(reportContents, "</MacAddresses>");
               this.appendNewLine(reportContents, "</List>");
            }
         }

         this.appendNewLine(reportContents, "</MACWhiteLists>");
         this.appendNewLine(reportContents, "<VLANs>");
         AxxMBTableMBean var87 = MMUUtil.getAxxMBTable(ethernetSwitchMoid, "BridgeConfigAndStatus.VlanConfigAndStatus.CurrentVLANs");
         if(var87 != null) {
            var85 = var87.getRows();
            MOID[] var92 = var85;
            var88 = var85.length;

            for(lanIndices = 0; lanIndices < var88; ++lanIndices) {
               wanIndices = var92[lanIndices];
               this.appendNewLine(reportContents, "<VLAN>");
               this.appendReportElement(reportContents, wanIndices, "VlanId", "VlanId");
               this.appendReportElement(reportContents, wanIndices, "VlanName", "VlanName");
               this.appendReportElement(reportContents, wanIndices, "ListOfMemberPorts", "ListOfMemberPorts");
               this.appendReportElement(reportContents, wanIndices, "StaticUntaggedPorts", "StaticUntaggedPorts");
               this.appendReportElement(reportContents, wanIndices, "UnregisteredMulticast", "UnregisteredMulticast");
               this.appendNewLine(reportContents, "</VLAN>");
            }
         }

         this.appendNewLine(reportContents, "</VLANs>");
         this.appendNewLine(reportContents, "<WANs>");
         var81 = RelationSearchUtil.getChild(ethernetMoid, "com.ericsson.mlne.im.fa.ethernet.WanInterface");
         MOID var110;
         if(var81 != null) {
            this.appendNewLine(reportContents, "<WAN>");
            this.appendReportElement(reportContents, var81, "MocMoi", "MocMoi");
            this.appendReportElement(reportContents, var81, "AliasName", "AliasName");
            this.appendReportElement(reportContents, var81, "WanStatusAndConfig.HoldOffTime", "HoldOffTime");
            this.appendReportElement(reportContents, var81, "WanStatusAndConfig.WaitToRestore", "WaitToRestore");
            this.appendReportElement(reportContents, var81, "WanStatusAndConfig.MinSpeed", "MinSpeed");
            this.appendReportElement(reportContents, var81, "WanStatusAndConfig.MaxSpeed", "MaxSpeed");
            var84 = "-";

            try {
               Object var96 = mgtServer.getAttribute(var81, "EthernetPm.EthernetPmConfig.PmView");
               if(var96 != null && !var96.toString().equals("")) {
                  AxxEnum var99 = (AxxEnum)var96;
                  String var100 = var99.getLabel();
                  var84 = var100.toString();
               }
            } catch (Exception var43) {
               ;
            }

            this.appendReportElement(reportContents, "PmView", var84);
            this.appendReportElement(reportContents, var81, "WanStatusAndConfig.PullDownInterfaceUsage", "PullDownInterfaceUsage");
            this.appendNewLine(reportContents, "<TrafficClassConfig>");
            AxxMBTableMBean var103 = MMUUtil.getAxxMBTable(var81, "TrafficClassConfig");
            if(var103 != null) {
               MOID[] var104 = var103.getRows();
               MOID[] var97 = var104;
               int var101 = var104.length;

               for(int var108 = 0; var108 < var101; ++var108) {
                  var110 = var97[var108];
                  this.appendNewLine(reportContents, "<TC>");
                  this.appendReportElement(reportContents, var110, "TrafficClass", "TrafficClass");
                  this.appendReportElement(reportContents, var110, "BufferSize", "BufferSize");
                  this.appendNewLine(reportContents, "</TC>");
               }
            }

            this.appendNewLine(reportContents, "</TrafficClassConfig>");
            this.appendNewLine(reportContents, "</WAN>");
         }

         this.appendNewLine(reportContents, "</WANs>");
         this.appendNewLine(reportContents, "<Layer1Connection>");
         var86 = new MOID("com.ericsson.mlne.im.fa.ethernet.EplService", (String)null, ethernetMoid.getResourceId());
         MOIDList var105 = MgtUtil.getInstance().getMgtService().queryMBeans(var86);
         if(var105 != null && var105.size() == 1) {
            ArrayList var107 = new ArrayList();
            ArrayList var98 = new ArrayList();
            ArrayList var102 = new ArrayList();
            ArrayList var109 = new ArrayList();
            var110 = (MOID)var105.get(0);
            AxxMBTableMBean var112 = MMUUtil.getAxxMBTable(var110, "TableOfAllEplServicesQoS");
            if(var112 != null) {
               this.appendNewLine(reportContents, "<Connections>");
               eplSize = var112.getRows();
               MOID[] var115 = eplSize;
               lanIdx = eplSize.length;

               for(int var117 = 0; var117 < lanIdx; ++var117) {
                  MOID oid = var115[var117];
                  Object lanWanMocMoi = MMUUtil.getAttribute(oid, "IndexInterface1");
                  Object eplQoSMoid = MMUUtil.getAttribute(oid, "IndexInterface2");
                  AxxEnum trafficClassMappingTableMBean = (AxxEnum)MMUUtil.getAttribute(oid, "Interface1");
                  AxxEnum rows = (AxxEnum)MMUUtil.getAttribute(oid, "Interface2");
                  String arr$ = trafficClassMappingTableMBean.getLabel();
                  String len$ = rows.getLabel();
                  var107.add((Integer)lanWanMocMoi);
                  var98.add((Integer)eplQoSMoid);
                  var102.add(arr$);
                  var109.add(len$);
                  String i$ = arr$ + " - " + len$;
                  this.appendNewLine(reportContents, "<Connection LanWanMocMoi=\"" + i$ + "\">");
                  this.appendReportElement(reportContents, oid, "Interface1", "Interface1");
                  this.appendReportElement(reportContents, oid, "Interface2", "Interface2");
                  this.appendReportElement(reportContents, oid, "LlfEnable", "LlfEnable");
                  this.appendReportElement(reportContents, oid, "EplConfigureQoS.DefaultUserPriority", "DefaultUserPriority");
                  this.appendReportElement(reportContents, oid, "EplConfigureQoS.TrustedPort", "TrustedPort");
                  this.appendReportElement(reportContents, oid, "EplConfigureQoS.PcpSelection", "PcpSelection");
                  this.appendReportElement(reportContents, oid, "EplConfigureQoS.MappingType", "MappingType");
                  this.appendReportElement(reportContents, oid, "EplConfigureQoS.SchedulingProfile", "SchedulingProfile");
                  this.appendReportElement(reportContents, oid, "LlfFaultPropagationIF1toIF2", "LlfFaultPropagationIF1toIF2");
                  this.appendReportElement(reportContents, oid, "LlfFaultPropagationIF2toIF1", "LlfFaultPropagationIF2toIF1");
                  this.appendNewLine(reportContents, "</Connection>");
               }

               this.appendNewLine(reportContents, "</Connections>");
            }

            this.appendNewLine(reportContents, "<Queues>");
            int var114 = var107.size();

            MOID r;
            Integer var116;
            Integer var118;
            String var119;
            String var120;
            MOID var121;
            AxxMBTableMBean var122;
            MOID[] var123;
            MOID[] var125;
            int var126;
            int var128;
            for(i = 0; i < var114; ++i) {
               var116 = (Integer)var107.get(i);
               var118 = (Integer)var98.get(i);
               var119 = var116 + "." + var118;
               var120 = (String)var102.get(i) + " - " + (String)var109.get(i);
               var121 = new MOID("com.ericsson.mlne.im.fa.ethernet.EplConfigureQoS", var119, var110.getResourceId());
               var122 = MMUUtil.getAxxMBTable(var121, "EplQueuesTable");
               if(var122 != null) {
                  this.appendNewLine(reportContents, "<EPL OID=\"" + var119 + "\" LanWanMocMoi=\"" + var120 + "\">");
                  var123 = var122.getRows();
                  var125 = var123;
                  var126 = var123.length;

                  for(var128 = 0; var128 < var126; ++var128) {
                     r = var125[var128];
                     String r1 = (String)MMUUtil.getAttribute(r, "NetworkPriority");
                     this.appendNewLine(reportContents, "<Queue>");
                     this.appendReportElement(reportContents, "Valid", r1 != null && !"".equals(r1) && !"N/A".equals(r1)?"Yes":"No");
                     this.appendReportElement(reportContents, r, "TC", "TC");
                     this.appendReportElement(reportContents, r, "NetworkPriority", "NetworkPriority");
                     this.appendReportElement(reportContents, r, "BufferSize", "BufferSize");
                     this.appendReportElement(reportContents, r, "Aging", "Aging");
                     this.appendReportElement(reportContents, r, "WredEnable", "WredEnable");
                     this.appendReportElement(reportContents, r, "SchedulerType", "SchedulerType");
                     this.appendReportElement4QueuesTable(reportContents, r, "WfqWeight", "WfqWeight");
                     this.appendReportElement4QueuesTable(reportContents, r, "ColorDropEnable", "ColorDropEnable");
                     this.appendReportElement4QueuesTable(reportContents, r, "YellowLimit", "YellowLimit");
                     this.appendNewLine(reportContents, "</Queue>");
                  }

                  this.appendNewLine(reportContents, "</EPL>");
               }
            }

            this.appendNewLine(reportContents, "</Queues>");
            this.appendNewLine(reportContents, "<UserToNetworkPriority>");

            for(i = 0; i < var114; ++i) {
               var116 = (Integer)var107.get(i);
               var118 = (Integer)var98.get(i);
               var119 = var116 + "." + var118;
               var120 = (String)var102.get(i) + " - " + (String)var109.get(i);
               var121 = new MOID("com.ericsson.mlne.im.fa.ethernet.EplStatusAndConfigL1", var119, var110.getResourceId());
               var122 = MMUUtil.getAxxMBTable(var121, "PriorityMapping");
               if(var122 != null) {
                  int var124 = 0;
                  this.appendNewLine(reportContents, "<EPL OID=\"" + var119 + "\" LanWanMocMoi=\"" + var120 + "\">");
                  var125 = var122.getRows();
                  MOID[] var127 = var125;
                  var128 = var125.length;

                  for(int var129 = 0; var129 < var128; ++var129) {
                     MOID var130 = var127[var129];
                     if(var124 % 8 == 0) {
                        this.appendNewLine(reportContents, "<Row>");
                     }

                     this.appendNewLine(reportContents, "<Mapping>");
                     this.appendReportElement(reportContents, var130, "UserPriority", "UserPriority");
                     this.appendReportElement(reportContents, var130, "NetworkPriority", "NetworkPriority");
                     this.appendNewLine(reportContents, "</Mapping>");
                     if((var124 + 1) % 8 == 0) {
                        this.appendNewLine(reportContents, "</Row>");
                     }

                     ++var124;
                  }

                  this.appendNewLine(reportContents, "</EPL>");
               }
            }

            this.appendNewLine(reportContents, "</UserToNetworkPriority>");
            this.appendNewLine(reportContents, "<NetworkPriorityTrafficClass>");

            for(i = 0; i < var114; ++i) {
               var116 = (Integer)var107.get(i);
               var118 = (Integer)var98.get(i);
               var119 = var116 + "." + var118;
               var120 = (String)var102.get(i) + " - " + (String)var109.get(i);
               var121 = new MOID("com.ericsson.mlne.im.fa.ethernet.EplConfigureQoS", var119, var110.getResourceId());
               var122 = MMUUtil.getAxxMBTable(var121, "TrafficClassMapping");
               if(var122 != null) {
                  this.appendNewLine(reportContents, "<EPL OID=\"" + var119 + "\" LanWanMocMoi=\"" + var120 + "\">");
                  var123 = var122.getRows();
                  var125 = var123;
                  var126 = var123.length;

                  for(var128 = 0; var128 < var126; ++var128) {
                     r = var125[var128];
                     this.appendNewLine(reportContents, "<Mapping>");
                     this.appendReportElement(reportContents, r, "NetworkPriority", "NetworkPriority");
                     this.appendReportElement(reportContents, r, "TrafficClass", "TrafficClass");
                     this.appendNewLine(reportContents, "</Mapping>");
                  }

                  this.appendNewLine(reportContents, "</EPL>");
               }
            }

            this.appendNewLine(reportContents, "</NetworkPriorityTrafficClass>");
         }

         this.appendNewLine(reportContents, "</Layer1Connection>");
         this.appendNewLine(reportContents, "</CNX10EthernetConfiguration>");
         return reportContents.toString();
      }
   }

   protected void appendReportElement4QueuesTable(StringBuffer strBuff, MOID moid, String attribute, String reportTag) {
      this.appendReportElement4QueuesTable(strBuff, moid, attribute, reportTag, (IReportValueConverter)null);
   }

   protected void appendReportElement4QueuesTable(StringBuffer strBuff, MOID moid, String attribute, String reportTag, IReportValueConverter converter) {
      String value = "-";
      if(this.notDefined(reportTag)) {
         reportTag = attribute;
      }

      value = this.getDisplayedMsgForAttr4QueuesTable(moid, attribute, converter, value);
      this.appendReportElement(strBuff, reportTag, value);
   }

   protected String getDisplayedMsgForAttr4QueuesTable(MOID moid, String attribute, IReportValueConverter converter, String value) {
      String targetAttributes = "ColorDropEnable:YellowLimit";
      int pcp = moid.getClassname().equals("com.ericsson.mlne.im.fa.ethernet_bridge.SwitchQueuesTable")?this.getNetworkPcpSelection(moid):this.getNetworkPcpSelection4Epl(moid);
      String networkPriority = "";
      AxxEnum schedulerType = null;

      try {
         schedulerType = (AxxEnum)MgtUtil.getInstance().getMgtService().getAttribute(moid, "SchedulerType");
         networkPriority = (String)MgtUtil.getInstance().getMgtService().getAttribute(moid, "NetworkPriority");
      } catch (OperationException var10) {
         var10.printStackTrace();
      } catch (MBeanException var11) {
         var11.printStackTrace();
      }

      if(attribute.equals("WfqWeight") && (networkPriority.equals("N/A") || schedulerType != null && schedulerType.value() == 1)) {
         return "N/A";
      } else {
         if(targetAttributes.indexOf(attribute) >= 0) {
            boolean colorDroppingYellowLimitEnabled = false;
            switch(pcp) {
            case 2:
               if(networkPriority.contains("4")) {
                  colorDroppingYellowLimitEnabled = true;
               }
               break;
            case 3:
               if(networkPriority.contains("4") || networkPriority.contains("2")) {
                  colorDroppingYellowLimitEnabled = true;
               }
               break;
            case 4:
               if(networkPriority.contains("4") || networkPriority.contains("2") || networkPriority.contains("0")) {
                  colorDroppingYellowLimitEnabled = true;
               }
               break;
            case 5:
               colorDroppingYellowLimitEnabled = true;
            }

            if(!colorDroppingYellowLimitEnabled) {
               return "N/A";
            }
         }

         return super.getDisplayedMsgForAttr(moid, attribute, converter, value);
      }
   }

   private int getNetworkPcpSelection(MOID mbeanArg) {
      MOID mbean = new MOID("com.ericsson.mlne.im.fa.ethernet_bridge.BridgeConfigAndStatus", "BridgeConfigAndStatus.EthernetSwitch", mbeanArg.getResourceId());
      Integer networkPcpSelection = Integer.valueOf(-1);

      try {
         networkPcpSelection = Integer.valueOf(((AxxEnum)MgtUtil.getInstance().getMgtService().getAttribute(mbean, "NetworkPcpSelection")).value());
      } catch (OperationException var5) {
         var5.printStackTrace();
      } catch (MBeanException var6) {
         var6.printStackTrace();
      }

      return networkPcpSelection.intValue();
   }

   private int getNetworkPcpSelection4Epl(MOID mbeanArg) {
      String oid = mbeanArg.getObjectId().toString();
      int index1 = oid.lastIndexOf(46);
      int index2 = oid.indexOf(46);
      int index3 = oid.indexOf(58);
      String name = oid.substring(0, index3 + 1);
      oid = oid.substring(index2 + 1, index1);
      oid = name + oid;
      MOID mbean = new MOID("com.ericsson.mlne.im.fa.ethernet.EplConfigureQoS", oid, mbeanArg.getResourceId());
      Integer networkPcpSelection = Integer.valueOf(-1);

      try {
         networkPcpSelection = Integer.valueOf(((AxxEnum)MgtUtil.getInstance().getMgtService().getAttribute(mbean, "PcpSelection")).value());
      } catch (OperationException var10) {
         var10.printStackTrace();
      } catch (MBeanException var11) {
         var11.printStackTrace();
      }

      return networkPcpSelection.intValue();
   }

   private class ForbiddenEgressPortsConverter implements IReportValueConverter {

      int switchPortSize = 0;


      public ForbiddenEgressPortsConverter(int switchPortSize) {
         this.switchPortSize = switchPortSize;
      }

      public Object convert(Object o) {
         if(o != null && o instanceof ByteArray) {
            byte[] array = ((ByteArray)o).byteValue();
            String result = "";

            for(int k = 0; k < this.switchPortSize; ++k) {
               if(BitsWrap.getBitValue(array, k)) {
                  result = result + (k + 1) + " ";
               }
            }

            if("".equals(result)) {
               result = "-";
            }

            return result;
         } else {
            return "-";
         }
      }
   }
}
