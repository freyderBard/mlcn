package com.ericsson.mlcn.im.rules;

import com.ericsson.mlcn.im.model.IproutingTableModel;
import com.ericsson.mlcraft.IMC;
import com.ericsson.mlcraft.common.license.FormSaveCallback;
import com.ericsson.mlne.im.rules.WarningException;
import com.ericsson.mlne.utils.MMUUtil;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import junit.framework.Assert;
import no.axxessit.client.gui.ApplicationContext;
import no.axxessit.client.gui.table.AxxMBTableModel;
import no.axxessit.client.util.MgtUtil;
import no.axxessit.common.Logger;
import no.axxessit.common.lang.TranslationManager;
import no.axxessit.common.val.AxxEnum;
import no.axxessit.common.val.AxxInteger;
import no.axxessit.im.common.IPAddress;
import no.axxessit.im.common.IPv4v6Address;
import no.axxessit.mgt.Attribute;
import no.axxessit.mgt.MBeanException;
import no.axxessit.mgt.MOID;
import no.axxessit.mgt.MoAttributeList;
import no.axxessit.mgt.OperationException;

public abstract class NECallback extends FormSaveCallback implements IMC {

   private static Logger log = Logger.getLogger();
   private static TranslationManager tm = TranslationManager.getInstance();
   private static final String ATTR_SNMP_MANAGER_TABLE = "SNMPManagerTable";
   private static final String ATTR_IP_ROUTING_TABLE = "IPRoutingTable";
   private static final String PATTERN = "[\\p{ASCII}&&[^\\\\\"\\n\\r]]+";
   private static final String NE_NAME_PATTERN = "[\\p{Alnum}[-\\.]]+";
   private static final String NE_NAME_FIRST_CHAR_PATTERN = "[\\p{Alpha}]";
   private static final String NE_NAME_LAST_CHAR_PATTERN = "[^-\\.]";
   private static final int NE_NAME_MAXIMUM_LEN = 24;
   private static final int MAXIMUM_LEN = 255;
   private static final String ZERO_IP = "0.0.0.0";
   private static final String[] invalidIPRanges = new String[]{"127", "169.254", "224-255"};
   private ApplicationContext context;


   public boolean validate(ApplicationContext appContext, MoAttributeList[] list) {
      this.context = appContext;
      boolean result = true;

      try {
         this.customValidate(appContext, list);
      } catch (NECallback.CheckFailedException var5) {
         this.showErrorMsg(var5.getMsg());
         result = false;
      } catch (WarningException var6) {
         result = false;
      } catch (Exception var7) {
         log.error(var7);
         result = false;
      }

      if(result) {
         result = super.validate(appContext, list);
      }

      return result;
   }

   private void showErrorMsg(String msg) {
      Assert.assertNotNull("Application can not be null", this.context);
      if(null != msg) {
         this.context.showErrorDialog(tm.getString("$MLTN.NE_CONF_ERR"), msg);
      }
   }

   protected void checkContinentAndCity(ApplicationContext appContext, MoAttributeList eqRoot) throws NECallback.CheckFailedException {
      Object o = eqRoot.getAttribute("TimeContinent").getValue();
      AxxEnum continent = (AxxEnum)o;
      boolean ret = continent.hashCode() != 0;
      if(!continent.isValid()) {
         throw new NECallback.CheckFailedException("Invalid value of Continent.");
      } else if(!ret) {
         throw new NECallback.CheckFailedException(tm.getString("$MLTN.NE_CONF_NO_CONTINENT_SELECTED"));
      } else {
         o = eqRoot.getAttribute("TimeCity").getValue();
         AxxEnum city = (AxxEnum)o;
         ret = city.hashCode() != 0;
         if(!city.isValid()) {
            throw new NECallback.CheckFailedException("Invalid value of City.");
         } else if(!ret) {
            throw new NECallback.CheckFailedException(tm.getString("$MLTN.NE_CONF_NO_CITY_SELECTED"));
         }
      }
   }

   protected void checkStaticRoutes(ApplicationContext appContext, MoAttributeList eqRoot) throws NECallback.CheckFailedException {
      AxxMBTableModel staticRouteTable = (AxxMBTableModel)eqRoot.getAttribute("IPRoutingTable").getValue();
      Map addedStaticRouteRows = staticRouteTable.getAddedRows();
      Object[] addedRows = addedStaticRouteRows.entrySet().toArray();
      this.checkReservedIPAddressForStaticRouting(addedRows);
      Map[] oldRows = staticRouteTable.getRows();

      for(int unMgmtRows = 0; unMgmtRows < oldRows.length; ++unMgmtRows) {
         IPAddress i$ = (IPAddress)oldRows[unMgmtRows].get("Destination");
         if(!IPAddress.validate(i$.toString())) {
            throw new NECallback.CheckFailedException(tm.getString("$MLTN.NE_CONF_ROUTE_DEST_IP") + " " + tm.getString("$MLTN.NE_CONF_INVALID_IP"));
         }

         IPAddress unMgmtRow = (IPAddress)oldRows[unMgmtRows].get("RouteMask");
         if(!this.isValidNetMask(unMgmtRow.toString())) {
            throw new NECallback.CheckFailedException(tm.getString("$MLTN.NE_CONF_ROUTE_MASK") + " " + tm.getString("$MLTN.NE_CONF_INVALID_SUBNET_MASK"));
         }

         IPAddress destination = (IPAddress)oldRows[unMgmtRows].get("Gateway");
         if(!IPAddress.validate(destination.toString()) || destination.equals("0.0.0.0")) {
            throw new NECallback.CheckFailedException(tm.getString("$MLTN.NE_CONF_ROUTE_GATEWAY_IP") + " " + tm.getString("$MLTN.NE_CONF_INVALID_IP"));
         }

         if(!this.checkRouteMaskTowardsRouteDest(i$.toString(), unMgmtRow.toString())) {
            throw new NECallback.CheckFailedException(tm.getString("$MLTN.NE_CONF_STATIC_ROUTING") + " " + tm.getString("$MLTN.NE_CONF_INVALID_ROUTE_MASK_AND_IP_COMBINATION"));
         }

         int routemask = 0;

         for(int gateway = 0; gateway < oldRows.length; ++gateway) {
            IPAddress typeofservice = (IPAddress)oldRows[gateway].get("Gateway");
            IPAddress row = (IPAddress)oldRows[gateway].get("RouteMask");
            IPAddress m = (IPAddress)oldRows[gateway].get("Destination");
            if(destination.equals(typeofservice) && unMgmtRow.equals(row) && i$.equals(m)) {
               ++routemask;
               if(routemask >= 2) {
                  throw new NECallback.CheckFailedException(tm.getString("$MLTN.NE_CONF_NO_DUPLICATE_ENTRIES_ALLOWED"));
               }
            }

            if(unMgmtRow.equals(row) && i$.equals(m) && i$.equals("0.0.0.0") && unMgmtRow.equals("0.0.0.0") && typeofservice != destination) {
               int destinationAdded = appContext.showWarningDialog(tm.getString("$MLTN.NE_CONF_DEFAULT_GATEWAY") + " " + tm.getString("$MLTN.NE_CONF_WARN_DEFAULT_GATEWAY_CHANGED"), tm.getString("$MLTN.NE_CONF_WARN"), 2);
               if(destinationAdded != 0) {
                  throw new NECallback.CheckFailedException();
               }
            }
         }
      }

      if(staticRouteTable instanceof IproutingTableModel) {
         ArrayList var23 = ((IproutingTableModel)staticRouteTable).getFilteredList();
         Iterator var24 = var23.iterator();

         while(var24.hasNext()) {
            LinkedHashMap var25 = (LinkedHashMap)var24.next();
            String var26 = var25.get("Destination").toString();
            String var27 = var25.get("RouteMask").toString();
            String var28 = var25.get("Gateway").toString();
            String var29 = var25.get("TypeOfService").toString();

            for(int var30 = 0; var30 < addedRows.length; ++var30) {
               Map var31 = (Map)((Entry)addedRows[var30]).getKey();
               String var32 = var31.get("Destination").toString();
               String routemaskAdded = var31.get("RouteMask").toString();
               String gatewayAdded = var31.get("Gateway").toString();
               String typeofserviceAdded = var31.get("TypeOfService").toString();
               if(var32.equals(var26) && routemaskAdded.equals(var27) && gatewayAdded.equals(var28) && typeofserviceAdded.equals(var29)) {
                  String invalidMsgRowsHeader = "\nDestination              RouteMask              Gateway";
                  String invalidMsgRows = "\n";
                  invalidMsgRows = invalidMsgRows.concat(var32);

                  int nrOfSpaces;
                  for(nrOfSpaces = 0; nrOfSpaces < 17 - var32.length(); ++nrOfSpaces) {
                     invalidMsgRows = invalidMsgRows.concat("  ");
                  }

                  invalidMsgRows = invalidMsgRows.concat(routemaskAdded);

                  for(nrOfSpaces = 0; nrOfSpaces < 17 - routemaskAdded.length(); ++nrOfSpaces) {
                     invalidMsgRows = invalidMsgRows.concat("  ");
                  }

                  invalidMsgRows = invalidMsgRows.concat(gatewayAdded);
                  invalidMsgRows = invalidMsgRows.concat("\n");
                  throw new NECallback.CheckFailedException(tm.getString("$MLTN.NE_CONF_STATIC_ROUTING") + " " + tm.getString("$MLTN.NE_CONF_STATIC_ROUTE_DUPLICATE_WITH_NO_NETMGMET") + "\n" + invalidMsgRowsHeader + invalidMsgRows);
               }
            }
         }
      }

   }

   private void checkReservedIPAddressForStaticRouting(Object[] addedRows) throws NECallback.CheckFailedException {
      for(int row = 0; row < addedRows.length; ++row) {
         Map m = (Map)((Entry)addedRows[row]).getKey();
         String dest = m.get("Destination").toString();
         if(dest.startsWith("224.0.0.") || dest.startsWith("127.")) {
            throw new NECallback.CheckFailedException(tm.getString("$MLTN.NE_CONF_ROUTE_DEST_IP") + " " + tm.getString("$MLTN.NE_CONF_IP_ADDRESS_RESERVED"));
         }
      }

   }

   private void resolveValidAddedRows(Object[] addedRows) throws NECallback.CheckFailedException {
      for(int row = 0; row < addedRows.length; ++row) {
         Map m = (Map)((Entry)addedRows[row]).getKey();
         String dest = m.get("Destination").toString();
         if("0.0.0.0".equals(dest)) {
            throw new NECallback.CheckFailedException(tm.getString("$MLTN.NE_CONF_ROUTE_DEST_IP") + " " + tm.getString("$MLTN.NE_CONF_INVALID_IP"));
         }
      }

   }

   protected void checkPMStartTime(ApplicationContext appContext, MoAttributeList eqRoot) throws NECallback.CheckFailedException {
      boolean ok = true;
      Object o = eqRoot.getAttribute("PMStartTime").getValue();
      String[] pmStartTime = o.toString().split(":");
      if(pmStartTime.length != 2) {
         ok = false;
      }

      if(ok) {
         int[] allowedMinutesValues = new int[]{0, 15, 30, 45};

         try {
            int nfe = (new Integer(pmStartTime[0])).intValue();
            int minutes = (new Integer(pmStartTime[1])).intValue();
            ok = nfe >= 0 && nfe <= 23 && Arrays.binarySearch(allowedMinutesValues, minutes) >= 0;
         } catch (NumberFormatException var9) {
            ok = false;
         }
      }

      if(!ok) {
         throw new NECallback.CheckFailedException(tm.getString("$MLTN.NE_CONF_PM_START_TIME") + " " + tm.getString("$MLTN.NE_CONF_INVALID_PM_START_TIME"));
      }
   }

   protected void checkAlarmFilterTime(ApplicationContext appContext, MoAttributeList eqRoot) throws NECallback.CheckFailedException {
      boolean ok = true;
      Object o = eqRoot.getAttribute("AlarmFilterTime").getValue();
      Double alarmFilterTime = null;

      try {
         alarmFilterTime = new Double(o.toString());
         if(alarmFilterTime.doubleValue() < 1.0D || alarmFilterTime.doubleValue() > 10.0D) {
            ok = false;
         }
      } catch (NumberFormatException var8) {
         ok = false;
      }

      if(ok) {
         Double tmpValidate1 = new Double(alarmFilterTime.doubleValue() * 2.0D);
         Integer tmpValidate2 = new Integer(tmpValidate1.intValue());
         ok = tmpValidate1.doubleValue() == tmpValidate2.doubleValue();
      }

      if(!ok) {
         throw new NECallback.CheckFailedException(tm.getString("$MLTN.NE_CONF_ALARM_FILTER_TIME") + " " + tm.getString("$MLTN.NE_CONF_INVALID_ALARM_FILTER_TIME"));
      }
   }

   protected void checkNTPServer(ApplicationContext appContext, MoAttributeList eqRoot) throws NECallback.CheckFailedException {
      Attribute att = eqRoot.getAttribute("DcnNTPAddress");
      if(att != null) {
         String ntpServer = att.getValue().toString();
         if(ntpServer.length() != 0 && !IPv4v6Address.validate(ntpServer) && !IPAddress.validateHostname(ntpServer)) {
            throw new NECallback.CheckFailedException("NTP Server Address must be a valid IP address, hostname or empty.");
         }
      }
   }

   protected void checkRemoteFTPServer(ApplicationContext appContext, MoAttributeList eqRoot) throws NECallback.CheckFailedException {
      boolean isValid;
      try {
         String e = (String)eqRoot.getAttribute("FTPAddress1").getValue();
         InetAddress a = InetAddress.getByName(e);
         isValid = !a.isMulticastAddress() && !a.isLoopbackAddress() && IPAddress.validate(e);
      } catch (UnknownHostException var6) {
         isValid = false;
      }

      if(!isValid) {
         throw new NECallback.CheckFailedException(tm.getString("$MLTN.NE_CONF_FTP_SERVER") + " " + tm.getString("$MLTN.NE_CONF_INVALID_IP"));
      } else {
         this.checkRemoteFTPUserName(eqRoot);
         this.checkRemoteFTPPassword(eqRoot);
      }
   }

   private void checkRemoteFTPPassword(MoAttributeList eqRoot) throws NECallback.CheckFailedException {
      if(eqRoot.getAttribute("FTPPassword1") != null && eqRoot.getAttribute("FTPPassword1").getValue() != null) {
         String password = eqRoot.getAttribute("FTPPassword1").getValue().toString();
         if(password.length() > 15) {
            throw new NECallback.CheckFailedException(tm.getString("$MLTN.NE_CONF_FTP_SERVER") + " " + tm.getString("$MLTN.NE_CONF_INVALID_FTP_PASSWORD_LENGTH"));
         }
      }

   }

   private void checkRemoteFTPUserName(MoAttributeList eqRoot) throws NECallback.CheckFailedException {
      if(eqRoot.getAttribute("FTPUserName1") != null && eqRoot.getAttribute("FTPUserName1").getValue() != null) {
         String userName = (String)eqRoot.getAttribute("FTPUserName1").getValue();
         if(userName.length() == 0 || userName.length() > 15) {
            throw new NECallback.CheckFailedException(tm.getString("$MLTN.NE_CONF_FTP_SERVER") + " " + tm.getString("$MLTN.NE_CONF_INVALID_FTP_USERNAME_LENGTH"));
         }

         if(!Pattern.matches("[\\p{ASCII}&&[^\\\\\"\\n\\r]]+", userName)) {
            throw new NECallback.CheckFailedException(tm.getString("$MLTN.NE_CONF_INVALID_FTP_USERNAME") + "\n" + tm.getString("$MLTN.NE_CONF_INVALID_CHARACTER"));
         }
      }

   }

   protected void checkDHCPAddress(ApplicationContext appContext, MoAttributeList eqRoot) throws NECallback.CheckFailedException {
      String dhcpAddress = (String)eqRoot.getAttribute("DHCPAddress").getValue();
      if(dhcpAddress != null) {
         if(dhcpAddress.length() != 0 && !IPv4v6Address.validate(dhcpAddress) && IPAddress.validateHostname(dhcpAddress)) {
            throw new NECallback.CheckFailedException("DHCP Relay Server Address must be a valid IP address, hostname or empty.");
         }
      }
   }

   protected void checkSNMPTrapReceivers(ApplicationContext appContext, MoAttributeList eqRoot) throws NECallback.CheckFailedException {
      AxxMBTableModel trapReceiverTable = (AxxMBTableModel)eqRoot.getAttribute("SNMPManagerTable").getValue();
      Map editedTrapReceiverRows = trapReceiverTable.getEditedRows();
      Map addedTrapReceiverRows = trapReceiverTable.getAddedRows();
      editedTrapReceiverRows.putAll(addedTrapReceiverRows);
      Object[] rows = editedTrapReceiverRows.entrySet().toArray();
      HashMap result = new HashMap();
      String ipKey = "IPAddress";
      String portKey = "Port";
      int ipAddressIndex = trapReceiverTable.getColumnIndex(ipKey);
      int portIndex = trapReceiverTable.getColumnIndex(portKey);
      int totalRowCount = trapReceiverTable.getRowCount();

      Map haveSavedIPAddressPort;
      for(int tableDetails = 0; tableDetails < totalRowCount; ++tableDetails) {
         if(!trapReceiverTable.isRowDeleted(tableDetails)) {
            HashMap IPAddressPortList = new HashMap();
            IPAddressPortList.put(ipKey, trapReceiverTable.getValueAt(tableDetails, ipAddressIndex));
            IPAddressPortList.put(portKey, trapReceiverTable.getValueAt(tableDetails, portIndex));
            haveSavedIPAddressPort = trapReceiverTable.getRow(tableDetails);
            haveSavedIPAddressPort.get("Id");
            result.put((MOID)trapReceiverTable.getRow(tableDetails).get("Id"), IPAddressPortList);
         }
      }

      Map var22 = Collections.synchronizedMap(result);

      MOID i;
      for(int var23 = 0; var23 < rows.length; ++var23) {
         haveSavedIPAddressPort = (Map)((Entry)rows[var23]).getKey();
         i = (MOID)haveSavedIPAddressPort.get("Id");
         var22.remove(i);
      }

      ArrayList var24 = new ArrayList();
      Iterator var25 = var22.keySet().iterator();

      while(var25.hasNext()) {
         i = (MOID)var25.next();
         var24.add(((Map)var22.get(i)).get("IPAddress") + ":" + ((Map)var22.get(i)).get("Port"));
      }

      LinkedList var26 = new LinkedList();
      Iterator var27 = var24.iterator();

      while(var27.hasNext()) {
         Object m = var27.next();
         if(var26.contains(m)) {
            throw new NECallback.CheckFailedException("The combination of IP address and Port for SNMP Manager must be unique.");
         }

         var26.add(m);
      }

      for(int var28 = 0; var28 < rows.length; ++var28) {
         Map var29 = (Map)((Entry)rows[var28]).getKey();
         IPv4v6Address ipObj = (IPv4v6Address)var29.get("IPAddress");
         ipObj = new IPv4v6Address(ipObj.toString());
         if(!ipObj.isValidAddress()) {
            throw new NECallback.CheckFailedException(tm.getString("$MLTN.NE_CONF_SNMP_MANAGER_IP") + " " + tm.getString("$MLTN.NE_CONF_INVALID_IP"));
         }

         if(ipObj.IsIPv6Address()) {
            throw new NECallback.CheckFailedException("\nThe node does not support IPv6.");
         }

         AxxInteger snmpManagerPort = (AxxInteger)var29.get("Port");
         IPAddress snmpManagerIP = ipObj.getIPv4Address();
         String IPAddressPort = snmpManagerIP.toString() + ":" + snmpManagerPort.value();
         if(var24.contains(IPAddressPort)) {
            throw new NECallback.CheckFailedException("The combination of IP address and Port for SNMP Manager must be unique.");
         }
      }

   }

   protected void checkDefaultGateway(ApplicationContext appContext, MoAttributeList eqRoot) throws MBeanException, NECallback.CheckFailedException {
      Object defaultGateway = eqRoot.getAttribute("IPDefaultGateway").getValue();
      boolean defaultGatewayOK = IPAddress.validate(defaultGateway.toString());
      if(!defaultGatewayOK) {
         throw new NECallback.CheckFailedException(tm.getString("$MLTN.NE_CONF_DEFAULT_GATEWAY") + " " + tm.getString("$MLTN.NE_CONF_INVALID_IP"));
      } else if(!this.isValidIPAddress(defaultGateway.toString())) {
         throw new NECallback.CheckFailedException(tm.getString("$MLTN.NE_CONF_DEFAULT_GATEWAY") + " " + tm.getString("$MLTN.NE_CONF_INVALID_IP_RANGE"));
      } else {
         IPAddress defGatewayCurrent = (IPAddress)MgtUtil.getInstance().getMgtService().getAttribute(eqRoot.getMOID(), "IPDefaultGateway");
         if(!defGatewayCurrent.equals((IPAddress)defaultGateway)) {
            int res = appContext.showWarningDialog(tm.getString("$MLTN.NE_CONF_WARN_DEFAULT_GATEWAY_CHANGED"), tm.getString("$MLTN.NE_CONF_WARN"), 2);
            if(res != 0) {
               throw new NECallback.CheckFailedException();
            }
         }

      }
   }

   protected void checkNEIPAddressAndSubnetMask(ApplicationContext appContext, MoAttributeList eqRoot) throws NECallback.CheckFailedException {
      String ipAttrName = "IPAddress";
      String ipValidationMsgEntry = "$MLTN.NE_CONF_IP_ADDRESS";
      if(MMUUtil.isCNX10(eqRoot.getMOID())) {
         ipValidationMsgEntry = "$MLTN.NE_CONF_CN210_IP_ADDRESS";
      }

      IPAddress ip = (IPAddress)eqRoot.getAttribute(ipAttrName).getValue();
      if(!this.isValidIPAddress(ip.toString())) {
         throw new NECallback.CheckFailedException(tm.getString(ipValidationMsgEntry) + " " + tm.getString("$MLTN.NE_CONF_INVALID_IP_RANGE"));
      } else {
         IPAddress subnetMask = (IPAddress)eqRoot.getAttribute("IPSubnetMask").getValue();
         if(!this.isValidNetMask(subnetMask.toString())) {
            throw new NECallback.CheckFailedException(tm.getString("$MLTN.NE_CONF_SUBNET_MASK") + " " + tm.getString("$MLTN.NE_CONF_INVALID_SUBNET_MASK"));
         } else if(!this.isValidNetMaskAndHostIPCombination(ip.toString(), subnetMask.toString())) {
            throw new NECallback.CheckFailedException(tm.getString("$MLTN.NE_CONF_INVALID_SUBNET_MASK_AND_IP_COMBINATION"));
         } else {
            try {
               IPAddress ipCurrent = (IPAddress)MgtUtil.getInstance().getMgtService().getAttribute(eqRoot.getMOID(), ipAttrName);
               if(!ipCurrent.equals(ip)) {
                  int e = appContext.showWarningDialog(tm.getString("$MLTN.NE_CONF_WARN_IP_ADDRESS_CHANGED"), tm.getString("$MLTN.NE_CONF_WARN"), 2);
                  if(e != 0) {
                     throw new NECallback.CheckFailedException();
                  }
               }

            } catch (OperationException var9) {
               var9.printStackTrace();
               throw new NECallback.CheckFailedException();
            } catch (MBeanException var10) {
               var10.printStackTrace();
               throw new NECallback.CheckFailedException();
            }
         }
      }
   }

   protected void checkLANIPAddressAndSubnetMask(ApplicationContext appContext, MoAttributeList siteLANAttr, MoAttributeList eqRoot) throws NECallback.CheckFailedException {
      IPAddress ip = (IPAddress)eqRoot.getAttribute("SiteLanIPAddress").getValue();
      if(!this.isValidIPAddress(ip.toString())) {
         throw new NECallback.CheckFailedException(tm.getString("$MLTN.NE_CONF_LAN_IP_ADDRESS") + " " + tm.getString("$MLTN.NE_CONF_INVALID_IP_RANGE"));
      } else {
         IPAddress subnetMask = (IPAddress)siteLANAttr.getAttribute("SubnetMask").getValue();
         if(!this.isValidNetMask(subnetMask.toString())) {
            throw new NECallback.CheckFailedException(tm.getString("$MLTN.NE_CONF_LAN_SUBNET_MASK") + " " + tm.getString("$MLTN.NE_CONF_INVALID_SUBNET_MASK"));
         } else if(!ip.equals("0.0.0.0") && !this.isValidNetMaskAndHostIPCombination(ip.toString(), subnetMask.toString())) {
            throw new NECallback.CheckFailedException(tm.getString("$MLTN.NE_CONF_INVALID_SUBNET_MASK_AND_IP_COMBINATION_LAN"));
         } else {
            try {
               MOID e1 = siteLANAttr.getMOID();
               IPAddress ipCurrent = (IPAddress)MgtUtil.getInstance().getAttribute(e1, "IPAddress");
               if(!ipCurrent.equals(ip)) {
                  int res = appContext.showWarningDialog(tm.getString("$MLTN.NE_CONF_LAN_IP_ADDRESS") + " " + tm.getString("$MLTN.NE_CONF_WARN_IP_ADDRESS_CHANGED"), tm.getString("$MLTN.NE_CONF_WARN"), 2);
                  if(res != 0) {
                     throw new NECallback.CheckFailedException();
                  }
               }

            } catch (OperationException var9) {
               var9.printStackTrace();
               throw new NECallback.CheckFailedException();
            } catch (Exception var10) {
               var10.printStackTrace();
               throw new NECallback.CheckFailedException();
            }
         }
      }
   }

   protected boolean checkNEContact(ApplicationContext appContext, MoAttributeList eqRoot) throws NECallback.CheckFailedException {
      String neContact = eqRoot.getAttribute("Contact").getValue().toString();
      int neContactLen = neContact.length();
      if(neContactLen > 0 && !Pattern.matches("[\\p{ASCII}&&[^\\\\\"\\n\\r]]+", neContact)) {
         appContext.showErrorDialog(tm.getString("$MLTN.NE_CONF_ERR"), tm.getString("$MLTN.NE_CONF_CONTACT") + " " + tm.getString("$MLTN.NE_CONF_INVALID_CHARACTER"));
         throw new NECallback.CheckFailedException();
      } else if(neContactLen > 255) {
         appContext.showErrorDialog(tm.getString("$MLTN.NE_CONF_ERR"), tm.getString("$MLTN.NE_CONF_CONTACT") + " " + tm.getString("$MLTN.NE_CONF_FIELD_TOO_LONG"));
         throw new NECallback.CheckFailedException();
      } else {
         return true;
      }
   }

   protected void checkNELocation(ApplicationContext appContext, MoAttributeList eqRoot) throws NECallback.CheckFailedException {
      String neLocation = eqRoot.getAttribute("NELocation").getValue().toString();
      int neLocationLen = neLocation.length();
      if(neLocationLen > 0 && !Pattern.matches("[\\p{ASCII}&&[^\\\\\"\\n\\r]]+", neLocation)) {
         appContext.showErrorDialog(tm.getString("$MLTN.NE_CONF_ERR"), tm.getString("$MLTN.NE_CONF_LOCATION") + " " + tm.getString("$MLTN.NE_CONF_INVALID_CHARACTER"));
         throw new NECallback.CheckFailedException();
      } else if(neLocationLen > 255) {
         appContext.showErrorDialog(tm.getString("$MLTN.NE_CONF_ERR"), tm.getString("$MLTN.NE_CONF_LOCATION") + " " + tm.getString("$MLTN.NE_CONF_FIELD_TOO_LONG"));
         throw new NECallback.CheckFailedException();
      }
   }

   protected void checkNEName(ApplicationContext appContext, MoAttributeList eqRoot) throws NECallback.CheckFailedException {
      String neName = eqRoot.getAttribute("Name").getValue().toString();
      int neNameLen = neName.length();
      if(neNameLen == 0) {
         throw new NECallback.CheckFailedException(tm.getString("$MLTN.NE_CONF_NAME") + " " + tm.getString("$MLTN.NE_CONF_EMPTY_FIELD_NOT_ALLOWED"));
      } else if(neNameLen > 24) {
         throw new NECallback.CheckFailedException(tm.getString("$MLTN.NE_CONF_NAME") + " " + tm.getString("$MLTN.NE_CONF_NAME_TOO_LONG"));
      } else if(!Pattern.matches("[\\p{Alnum}[-\\.]]+", neName)) {
         throw new NECallback.CheckFailedException(tm.getString("$MLTN.NE_CONF_NAME") + " " + tm.getString("$MLTN.NE_CONF_INVALID_NAME"));
      } else if(!Pattern.matches("[\\p{Alpha}]", neName.substring(0, 1))) {
         throw new NECallback.CheckFailedException(tm.getString("$MLTN.NE_CONF_NAME") + " " + tm.getString("$MLTN.NE_CONF_INVALID_NAME_FIRST_CHAR"));
      } else if(!Pattern.matches("[^-\\.]", neName.substring(neNameLen - 1, neNameLen))) {
         throw new NECallback.CheckFailedException(tm.getString("$MLTN.NE_CONF_NAME") + " " + tm.getString("$MLTN.NE_CONF_INVALID_NAME_LAST_CHAR"));
      }
   }

   private boolean isValidIPAddress(String ipAddress) {
      for(int i = 0; i < invalidIPRanges.length; ++i) {
         if(!invalidIPRanges[i].contains("-")) {
            if(ipAddress.startsWith(invalidIPRanges[i])) {
               return false;
            }
         } else {
            try {
               if(!ipAddress.trim().equals("")) {
                  Integer e = Integer.valueOf(Integer.parseInt(ipAddress.split("\\.")[0]));
                  String[] invalidRange = invalidIPRanges[i].split("-");
                  Integer invalidMin = Integer.valueOf(Integer.parseInt(invalidRange[0]));
                  Integer invalidMax = Integer.valueOf(Integer.parseInt(invalidRange[1]));
                  if(e.intValue() >= invalidMin.intValue() && e.intValue() <= invalidMax.intValue()) {
                     return false;
                  }
               }
            } catch (NumberFormatException var7) {
               ;
            }
         }
      }

      return true;
   }

   private boolean isValidNetMask(String mask) {
      if(mask.equals("")) {
         return false;
      } else {
         String[] tmpArray = mask.toString().split("\\.");
         int[] netMaskArray = new int[tmpArray.length];

         for(int allowedNetMaskArray = 0; allowedNetMaskArray < tmpArray.length; ++allowedNetMaskArray) {
            netMaskArray[allowedNetMaskArray] = Integer.parseInt(tmpArray[allowedNetMaskArray]);
         }

         int[] var7 = new int[]{255, 254, 252, 248, 240, 224, 192, 128, 0};
         int byteNo = 0;

         label38:
         while(byteNo < netMaskArray.length) {
            if(byteNo < netMaskArray.length - 1 && netMaskArray[byteNo] < 255 && netMaskArray[byteNo + 1] > 0) {
               return false;
            }

            for(int netMaskNo = 0; netMaskNo < var7.length; ++netMaskNo) {
               if(netMaskArray[byteNo] == var7[netMaskNo]) {
                  ++byteNo;
                  continue label38;
               }
            }

            return false;
         }

         return true;
      }
   }

   private boolean isValidNetMaskAndHostIPCombination(String hostIP, String netMask) {
      String[] tmp_netMaskArray = netMask.split("\\.");
      String[] tmp_hostIPArray = hostIP.split("\\.");
      int[] netMaskArray = new int[4];
      int[] hostIPArray = new int[4];

      int byteNo;
      for(byteNo = 0; byteNo < 4; ++byteNo) {
         netMaskArray[byteNo] = Integer.parseInt(tmp_netMaskArray[byteNo]);
      }

      for(byteNo = 0; byteNo < 4; ++byteNo) {
         hostIPArray[byteNo] = Integer.parseInt(tmp_hostIPArray[byteNo]);
      }

      for(byteNo = 0; byteNo < 4; ++byteNo) {
         if(netMaskArray[byteNo] < 254) {
            int step = 255 - netMaskArray[byteNo];
            int tmp = 0;

            int i;
            while(hostIPArray[byteNo] != tmp) {
               if(hostIPArray[byteNo] == tmp + step) {
                  if(byteNo < 3) {
                     for(i = byteNo + 1; i < 4; ++i) {
                        if(hostIPArray[i] != 255) {
                           return true;
                        }
                     }

                     return false;
                  }

                  return false;
               }

               tmp += step + 1;
               if(tmp >= 255) {
                  return true;
               }
            }

            if(byteNo < 3) {
               for(i = byteNo + 1; i < 4; ++i) {
                  if(hostIPArray[i] != 0) {
                     return true;
                  }
               }

               return false;
            }

            return false;
         }
      }

      return true;
   }

   private boolean checkRouteMaskTowardsRouteDest(String dest, String mask) {
      String[] tmp_destArray = dest.split("\\.");
      String[] tmp_maskArray = mask.split("\\.");
      int[] destArray = new int[4];
      int[] maskArray = new int[4];

      for(int i = 0; i < 4; ++i) {
         destArray[i] = Integer.parseInt(tmp_destArray[i]);
         maskArray[i] = Integer.parseInt(tmp_maskArray[i]);
         if((destArray[i] & maskArray[i]) != destArray[i]) {
            return false;
         }
      }

      return true;
   }

   protected void checkSpeedTowardsAutoNeg(ApplicationContext appContext, MoAttributeList eqRoot) {
      Attribute autoNeg = eqRoot.getAttribute("AutoNegotiate");
      Attribute speed = eqRoot.getAttribute("Speed");
      MOID siteLanMoid = eqRoot.getMOID();
      MgtUtil e;
      AxxEnum currentAutoNegValue;
      AxxEnum currentSpeed;
      if(autoNeg != null && ((AxxEnum)speed.getValue()).value() == 1 && ((AxxEnum)autoNeg.getValue()).value() == 2) {
         try {
            e = MgtUtil.getInstance();
            currentAutoNegValue = (AxxEnum)e.getAttribute(siteLanMoid, "AutoNegotiate");
            currentSpeed = (AxxEnum)e.getAttribute(siteLanMoid, "Speed");
            if(currentAutoNegValue.value() != 1 || currentSpeed.value() != 1) {
               throw new NECallback.CheckFailedException(tm.getString("$ETHERNET.SPEED_TO_AUTO_WHEN_AUTO_NEG_OFF"));
            }
         } catch (Exception var10) {
            log.error(var10);
         }
      }

      if(autoNeg != null && ((AxxEnum)speed.getValue()).value() != 1 && ((AxxEnum)autoNeg.getValue()).value() == 1) {
         try {
            e = MgtUtil.getInstance();
            currentAutoNegValue = (AxxEnum)e.getAttribute(siteLanMoid, "AutoNegotiate");
            currentSpeed = (AxxEnum)e.getAttribute(siteLanMoid, "Speed");
            if(currentSpeed.value() != ((AxxEnum)speed.getValue()).value() && (currentAutoNegValue.value() != 1 || currentSpeed.value() == 1)) {
               throw new NECallback.CheckFailedException(tm.getString("$ETHERNET.SPEED_NOT_AUTO_WHEN_AUTO_NEG_ON"));
            }
         } catch (Exception var9) {
            log.error(var9);
         }
      }

   }

   abstract void customValidate(ApplicationContext var1, MoAttributeList[] var2) throws NECallback.CheckFailedException, MBeanException, WarningException;


   class CheckFailedException extends Exception {

      private static final long serialVersionUID = 5308127638358871965L;
      private String msg = null;


      public CheckFailedException() {}

      public CheckFailedException(String msg) {
         this.msg = msg;
      }

      public String getMsg() {
         return this.msg;
      }
   }
}
