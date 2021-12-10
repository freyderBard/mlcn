package com.ericsson.mlcn.im.physical;

import com.ericsson.mlcraft.common.license.FormSaveCallback;
import com.ericsson.mlcraft.common.util.BitsWrap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import no.axxessit.client.gui.ApplicationContext;
import no.axxessit.client.gui.table.AxxMBTableModel;
import no.axxessit.client.util.MgtUtil;
import no.axxessit.common.Logger;
import no.axxessit.common.as.ServiceLocator;
import no.axxessit.common.val.AxxEnum;
import no.axxessit.im.common.ByteArray;
import no.axxessit.mgt.Attribute;
import no.axxessit.mgt.AttributeList;
import no.axxessit.mgt.AxxMBTableMBean;
import no.axxessit.mgt.MBeanException;
import no.axxessit.mgt.MBeanProxy;
import no.axxessit.mgt.MOID;
import no.axxessit.mgt.ManagementServer;
import no.axxessit.mgt.MoAttributeList;
import no.axxessit.mgt.OperationException;
import no.axxessit.mgt.TableRef;

public class UserOutputCallback extends FormSaveCallback {

   private static final Logger log = Logger.getLogger();
   private static HashMap severityToBitsPosMapper = new HashMap();
   private static final int ALARMSEVERITY_CONTROLLED = 0;
   private static final int OPERATOR_CONTROLLED_ACTIVE = 1;
   private static final int OPERATOR_CONTROLLED_INACTIVE = 2;
   private static final int ACTIVE = 1;
   private static final String SEVERITY_BITS_ATTR = "SeverityBits";
   private static final String CONTROLLED_BY_ATTR = "ControlledBy";
   private static final String USER_OUTPUT_TABLE = "UserOutputTable";
   private static final String ENABLE_ATTR = "Enable";
   private static final String ACTIVE_ATTR = "Active";
   private static final String ALARM_SEVERITY_CLEARED_ATTR = "AlarmSeverityCleared";
   private static final String ALARM_SEVERITY_CRITICAL_ATTR = "AlarmSeverityCritical";
   private static final String ALARM_SEVERITY_MAJOR_ATTR = "AlarmSeverityMajor";
   private static final String ALARM_SEVERITY_MINOR_ATTR = "AlarmSeverityMinor";
   private static final String ALARM_SEVERITY_WARNING_ATTR = "AlarmSeverityWarning";


   public UserOutputCallback() {
      severityToBitsPosMapper.put("AlarmSeverity_Operator_Controlled", new Integer(0));
      severityToBitsPosMapper.put("ASC_Cleared", new Integer(5));
      severityToBitsPosMapper.put("ASC_Critical", new Integer(4));
      severityToBitsPosMapper.put("ASC_Major", new Integer(3));
      severityToBitsPosMapper.put("ASC_Minor", new Integer(2));
      severityToBitsPosMapper.put("ASC_Warning", new Integer(1));
   }

   public boolean validate(ApplicationContext appContext, MoAttributeList[] list) {
      boolean severitySet = false;
      boolean severityWrite = false;
      boolean write = false;
      boolean activeStateInfoTextShown = false;
      new ByteArray();
      MOID[] moids = null;
      byte[] temp = new byte[]{(byte)0};
      AxxMBTableModel table = (AxxMBTableModel)list[0].getAttribute("UserOutputTable").getValue();
      ServiceLocator locator = ServiceLocator.getInstance();
      ManagementServer managementServer = (ManagementServer)locator.lookup("ManagementServer", true);

      try {
         AxxMBTableMBean editedRows = (AxxMBTableMBean)MBeanProxy.createInstance(table.getMOID(), managementServer);
         moids = editedRows.getRows();
      } catch (ClassNotFoundException var27) {
         var27.printStackTrace();
      } catch (Exception var28) {
         var28.printStackTrace();
      }

      Object[] var31 = table.getEditedRows().entrySet().toArray();

      for(int i = 0; i < var31.length; ++i) {
         severitySet = false;
         severityWrite = false;
         write = false;
         MOID rowMoid = null;
         Map m = (Map)((Entry)var31[i]).getKey();

         ByteArray severityBits;
         try {
            rowMoid = (MOID)m.get("Id");
            severityBits = (ByteArray)m.get("SeverityBits");
            temp = severityBits.byteValue();
         } catch (OperationException var26) {
            log.error(var26);
         }

         Boolean operatorControlledOnNode = Boolean.valueOf(BitsWrap.getBitValue(temp, ((Integer)severityToBitsPosMapper.get("AlarmSeverity_Operator_Controlled")).intValue()));
         AxxEnum choosenControlInGUI = (AxxEnum)m.get("ControlledBy");
         Boolean enabledValueInGUI = (Boolean)m.get("Enable");
         AxxEnum activeValueInGUI = (AxxEnum)m.get("Active");
         if(choosenControlInGUI.value() == 1 && !((Boolean)m.get("Enable")).booleanValue()) {
            appContext.showErrorDialog("Can\'t activate if user output is disabled.");
            return false;
         }

         if(choosenControlInGUI.value() == 0) {
            BitsWrap.setBitValue(temp, ((Integer)severityToBitsPosMapper.get("ASC_Cleared")).intValue(), ((Boolean)m.get("AlarmSeverityCleared")).booleanValue());
            BitsWrap.setBitValue(temp, ((Integer)severityToBitsPosMapper.get("ASC_Critical")).intValue(), ((Boolean)m.get("AlarmSeverityCritical")).booleanValue());
            BitsWrap.setBitValue(temp, ((Integer)severityToBitsPosMapper.get("ASC_Major")).intValue(), ((Boolean)m.get("AlarmSeverityMajor")).booleanValue());
            BitsWrap.setBitValue(temp, ((Integer)severityToBitsPosMapper.get("ASC_Minor")).intValue(), ((Boolean)m.get("AlarmSeverityMinor")).booleanValue());
            BitsWrap.setBitValue(temp, ((Integer)severityToBitsPosMapper.get("ASC_Warning")).intValue(), ((Boolean)m.get("AlarmSeverityWarning")).booleanValue());
            if(BitsWrap.getBitValue(temp, ((Integer)severityToBitsPosMapper.get("ASC_Cleared")).intValue()) || BitsWrap.getBitValue(temp, ((Integer)severityToBitsPosMapper.get("ASC_Critical")).intValue()) || BitsWrap.getBitValue(temp, ((Integer)severityToBitsPosMapper.get("ASC_Major")).intValue()) || BitsWrap.getBitValue(temp, ((Integer)severityToBitsPosMapper.get("ASC_Minor")).intValue()) || BitsWrap.getBitValue(temp, ((Integer)severityToBitsPosMapper.get("ASC_Warning")).intValue())) {
               severitySet = true;
            }

            write = true;
            severityWrite = true;
         }

         if(choosenControlInGUI.value() == 0 && !severitySet) {
            appContext.showErrorDialog("You must choose at least one alarm severity if Alarm Severity Controlled is chosen.");
            return false;
         }

         if(operatorControlledOnNode.booleanValue() && choosenControlInGUI.value() == 0) {
            BitsWrap.setBitValue(temp, ((Integer)severityToBitsPosMapper.get("AlarmSeverity_Operator_Controlled")).intValue(), false);
            write = true;
         } else if(!operatorControlledOnNode.booleanValue() && choosenControlInGUI.value() != 0) {
            BitsWrap.setBitValue(temp, ((Integer)severityToBitsPosMapper.get("ASC_Cleared")).intValue(), false);
            BitsWrap.setBitValue(temp, ((Integer)severityToBitsPosMapper.get("ASC_Critical")).intValue(), false);
            BitsWrap.setBitValue(temp, ((Integer)severityToBitsPosMapper.get("ASC_Major")).intValue(), false);
            BitsWrap.setBitValue(temp, ((Integer)severityToBitsPosMapper.get("ASC_Minor")).intValue(), false);
            BitsWrap.setBitValue(temp, ((Integer)severityToBitsPosMapper.get("ASC_Warning")).intValue(), false);
            BitsWrap.setBitValue(temp, ((Integer)severityToBitsPosMapper.get("AlarmSeverity_Operator_Controlled")).intValue(), true);
            write = true;
            severityWrite = true;
         }

         if(!enabledValueInGUI.booleanValue() && (!activeStateInfoTextShown && choosenControlInGUI.value() == 2 && activeValueInGUI.value() == 1 || !operatorControlledOnNode.booleanValue() && choosenControlInGUI.value() == 2)) {
            appContext.showMessageDialog("Active state won\'t be saved when port is disabled. Active state will be set by the node.");
            activeStateInfoTextShown = true;
         }

         severityBits = new ByteArray(temp);
         if(write) {
            try {
               MOID[] e1 = moids;
               int len$ = moids.length;

               for(int i$ = 0; i$ < len$; ++i$) {
                  MOID moid = e1[i$];
                  if(rowMoid != null && moid.equals(rowMoid)) {
                     Attribute attrSeverityBits = new Attribute("SeverityBits", severityBits);
                     if(severityWrite) {
                        MgtUtil.getInstance().getMgtService().setAttribute(moid, attrSeverityBits);
                     }
                     break;
                  }
               }
            } catch (OperationException var29) {
               var29.printStackTrace();
            } catch (MBeanException var30) {
               var30.printStackTrace();
            }
         }
      }

      return super.validate(appContext, list);
   }

   public void postSave(ApplicationContext appContext, MoAttributeList[] moList) {
      MoAttributeList auxInput = this.getLicenseMoAttrList(moList[0].getMOID(), "UserInputTable");
      if(auxInput != null) {
         moList[0] = auxInput;
      } else {
         MoAttributeList auxOutput = this.getLicenseMoAttrList(moList[0].getMOID(), "UserOutputTable");
         moList[0] = auxOutput;
      }

      super.postSave(appContext, moList);
   }

   private MoAttributeList getLicenseMoAttrList(MOID mlcnMoid, String attrName) {
      AxxMBTableMBean model = this.fetchMBTable(mlcnMoid, attrName);
      if(model != null && model.getRows().length >= 1) {
         AttributeList attrList = new AttributeList();
         attrList.add(new Attribute(attrName, model));
         return new MoAttributeList(model.getRows()[0], attrList);
      } else {
         return null;
      }
   }

   private AxxMBTableMBean fetchMBTable(MOID mlcnMoid, String tableName) {
      ManagementServer server = MgtUtil.getInstance().getMgtService();
      TableRef tr = null;

      try {
         Object e = server.getAttribute(mlcnMoid, tableName);
         if(e instanceof TableRef) {
            tr = (TableRef)e;
            return (AxxMBTableMBean)MBeanProxy.createInstance(tr.getMoid(), server);
         }
      } catch (Exception var6) {
         log.error(var6);
      }

      return null;
   }

}
