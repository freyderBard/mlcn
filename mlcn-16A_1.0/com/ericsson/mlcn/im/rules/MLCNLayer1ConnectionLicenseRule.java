package com.ericsson.mlcn.im.rules;

import com.ericsson.mlcraft.common.util.EnhancedAbstractDefaultRule;
import com.ericsson.mlne.im.fa.ethernet.ConfigureEplCallback;
import no.axxessit.client.util.MgtUtil;
import no.axxessit.common.Logger;
import no.axxessit.im.util.IMUtil;
import no.axxessit.mgt.AttributeList;
import no.axxessit.mgt.MOID;
import no.axxessit.mgt.rule.inf.IContextProvider;

public class MLCNLayer1ConnectionLicenseRule extends EnhancedAbstractDefaultRule {

   protected static Logger log = Logger.getLogger(ConfigureEplCallback.class);


   public AttributeList evaluate(Object sourceContext, AttributeList list, IContextProvider callback, Object userData) {
      if(sourceContext != null && sourceContext instanceof MOID) {
         MOID sourceMoid = (MOID)sourceContext;
         MOID neMoid = IMUtil.getInstance().getNe(sourceMoid);
         if(this.isCN210Release1(neMoid)) {
            this.hideAttributes(sourceContext, new String[]{this.getDestinationAttributeName()});
         }
      }

      return null;
   }

   public String getDestinationAttributeName() {
      return "licenseIcon";
   }

   public String[] getSourceAttributeNames() {
      return new String[]{"ConfigVariant"};
   }

   private boolean isCN210Release1(MOID m) {
      if(m.getClassname().toString().equals("com.ericsson.mlcn.im.MLCN")) {
         try {
            return ((Boolean)MgtUtil.getInstance().getAttribute(m, "IsCN210_1_0")).booleanValue();
         } catch (Exception var3) {
            log.error(var3);
            return false;
         }
      } else {
         return false;
      }
   }

}
