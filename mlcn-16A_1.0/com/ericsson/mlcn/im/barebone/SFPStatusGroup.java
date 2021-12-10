package com.ericsson.mlcn.im.barebone;

import com.ericsson.mlcraft.client.gui.forms.nodes.attributegroup.FieldInfo;
import com.ericsson.mlcraft.client.gui.forms.nodes.attributegroup.LabelOptions;
import com.ericsson.mlcraft.client.gui.forms.nodes.attributegroup.ValueOptions;
import com.ericsson.mlcraft.common.util.RelationSearchUtil;
import com.ericsson.mlcraft.im.common.AttributeGroupHelper;
import java.util.List;
import no.axxessit.common.val.AxxEnum;
import no.axxessit.mgt.MBeanContext;
import no.axxessit.mgt.MBeanException;
import no.axxessit.mgt.MOID;
import no.axxessit.mgt.MOIDList;
import no.axxessit.mgt.OperationException;

public class SFPStatusGroup extends AttributeGroupHelper {

   public SFPStatusGroup(String id, MOIDList moids, MBeanContext context) {
      super(id, (String)null, moids, context);
   }

   protected void addFieldInfo(List fieldInfo) {
      MOID mlcn = (MOID)this.moids.get(0);
      MOIDList sfpMoids = RelationSearchUtil.getChildren(mlcn, "com.ericsson.mlne.im.common.physical.SFP", 5);
      LabelOptions headerOptions;
      if(sfpMoids.size() > 0) {
         sfpMoids.removeDuplicates();
         this.sort(sfpMoids);
         headerOptions = new LabelOptions(true, 0, 1.0D, (String)null);
         LabelOptions alarmsAndStatusLabelOptions = new LabelOptions(false, 2, 1.0D, "#VIEW:%env.mlcn.mlcn_sfp_alarms_and_status%");
         LabelOptions configureLabelOptions = new LabelOptions(false, 2, 1.0D, "#VIEW:%env.mlcn.mlcn_sfp_configure%");
         LabelOptions labelOptions = new LabelOptions(false);
         LabelOptions labelOptions2 = new LabelOptions(false, 0, 1.0D, (String)null);
         ValueOptions alarmValueOptions = new ValueOptions(false, 2, 1.0D, (String)null);
         fieldInfo.add(new FieldInfo(FieldInfo.SPACE));
         fieldInfo.add(new FieldInfo(FieldInfo.LABEL, "$MLCN.SFP_OPER_STATUS", headerOptions));
         fieldInfo.add(new FieldInfo(FieldInfo.LABEL, "$MLCN.SFP_HARDWARE_Error", headerOptions));
         fieldInfo.add(new FieldInfo(FieldInfo.LABEL, "$MLCN.SFP_CONFIGURE", headerOptions));

         for(int i = 0; i < sfpMoids.size(); ++i) {
            MOID sfp = (MOID)sfpMoids.get(i);
            String mocmoi = this.getAttr(sfp, "MocMoi");
            fieldInfo.add(new FieldInfo(FieldInfo.LABEL, mocmoi, sfp, alarmsAndStatusLabelOptions));
            String operStatus = this.getAttr(sfp, "OperStatus");
            fieldInfo.add(new FieldInfo(FieldInfo.LABEL, operStatus, sfp, labelOptions2));
            fieldInfo.add(new FieldInfo(FieldInfo.ALARM, (String)null, sfp, "HardwareError", "MLCraft Severity", labelOptions, alarmValueOptions));
            fieldInfo.add(new FieldInfo(FieldInfo.LABEL, "Configure", sfp, configureLabelOptions));
         }
      } else {
         headerOptions = new LabelOptions(false, 2, 1.0D, (String)null);
         fieldInfo.add(new FieldInfo(FieldInfo.SPACE));
         fieldInfo.add(new FieldInfo(FieldInfo.LABEL, "No SFP module is plugged in.", headerOptions));
         fieldInfo.add(new FieldInfo(FieldInfo.SPACE));
         fieldInfo.add(new FieldInfo(FieldInfo.SPACE));
      }

   }

   private String getAttr(MOID moid, String attrName) {
      String result = null;

      try {
         Object e = this.context.getManagementServer().getAttribute(moid, attrName);
         if(e != null) {
            if(e instanceof AxxEnum) {
               result = ((AxxEnum)e).getLabel();
            } else if(e instanceof String) {
               result = (String)e;
            } else {
               result = e.toString();
            }
         }
      } catch (OperationException var5) {
         ;
      } catch (MBeanException var6) {
         ;
      }

      return result;
   }

   public String getTitle() {
      return super.getTitle();
   }
}
