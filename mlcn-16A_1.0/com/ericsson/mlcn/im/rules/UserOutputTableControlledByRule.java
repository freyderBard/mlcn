package com.ericsson.mlcn.im.rules;

import com.ericsson.mlcraft.common.util.EnhancedAbstractDefaultRule;
import no.axxessit.mgt.Attribute;
import no.axxessit.mgt.AttributeList;
import no.axxessit.mgt.rule.inf.IContextProvider;

public class UserOutputTableControlledByRule extends EnhancedAbstractDefaultRule {

   private final String[] ALARM_SEVERITY_CONTROLLED_ATTRIBUTES = new String[]{"AlarmSeverityCleared", "AlarmSeverityCritical", "AlarmSeverityMajor", "AlarmSeverityMinor", "AlarmSeverityWarning"};
   private final String ATTR_CONTROLLED_BY = "ControlledBy";


   public AttributeList evaluate(Object sourceContext, AttributeList list, IContextProvider callback, Object userData) {
      Attribute attrControlledBy = list.getAttribute("ControlledBy");
      if(!attrControlledBy.getValue().toString().equals("Alarm Severity(0)")) {
         this.disableAttributes(sourceContext, this.ALARM_SEVERITY_CONTROLLED_ATTRIBUTES);
      } else {
         this.enableAttributes(sourceContext, this.ALARM_SEVERITY_CONTROLLED_ATTRIBUTES);
      }

      return null;
   }

   public String[] getSourceAttributeNames() {
      return new String[]{"ControlledBy"};
   }

   public String getDestinationAttributeName() {
      return "ControlledBy";
   }
}
