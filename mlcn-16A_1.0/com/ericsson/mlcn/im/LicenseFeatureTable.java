package com.ericsson.mlcn.im;

import com.ericsson.mlne.im.common.MrBean;
import java.util.StringTokenizer;
import no.axxessit.common.Logger;
import no.axxessit.mgt.MBeanInfo;

public class LicenseFeatureTable extends MrBean {

   private static final long serialVersionUID = 1L;
   protected static Logger log = Logger.getLogger();


   public LicenseFeatureTable(MBeanInfo info) {
      super(info);
   }

   public String getNumber() {
      String idx = this.getIndexString();
      StringTokenizer hexValues = new StringTokenizer(idx, ".");
      String productNumber = new String();
      if(hexValues.countTokens() <= 2) {
         log.error("Parsing product number failed. Idx:" + idx + " is not proper index of a license.");
         return null;
      } else {
         hexValues.nextToken();

         while(hexValues.hasMoreTokens()) {
            String token = hexValues.nextToken();

            try {
               productNumber = productNumber + (char)Integer.parseInt(token);
            } catch (NumberFormatException var6) {
               log.error("Parsing product number failed. Idx:" + idx + " is not proper index of a license.");
               return null;
            }
         }

         return productNumber;
      }
   }

}
