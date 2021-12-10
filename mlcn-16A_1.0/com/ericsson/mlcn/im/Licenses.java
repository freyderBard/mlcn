package com.ericsson.mlcn.im;

import com.ericsson.mlcraft.common.util.BITS_BitSet;
import com.ericsson.mlne.im.common.MrBean;
import java.util.Hashtable;
import java.util.regex.Pattern;
import no.axxessit.common.Logger;
import no.axxessit.mgt.AttRef;
import no.axxessit.mgt.MBeanInfo;
import no.axxessit.mgt.MOID;
import no.axxessit.mgt.OperationException;

public class Licenses extends MrBean {

   private static final long serialVersionUID = 1L;
   private static final String NULL_STRING = "";
   private static final String ZERO_STRING = "0";
   private final String ALPHA_NUMERIC_PATTERN = "[\\p{Alnum}\\s]+";
   private static final String ATTR_FINGERPRINT = "FingerPrint";


   public Licenses(MBeanInfo info) {
      super(info);
   }

   public String getLicenseStatus() {
      byte notStarted = 1;
      byte started = 2;
      byte expired = 3;
      int status = ((Integer)this.resource.getAttribute(this.moid, "GraceStatus")).intValue();
      if(status == 1) {
         return "OK";
      } else if(status == expired) {
         return "License missing";
      } else if(status == started) {
         int seconds = ((Integer)this.resource.getAttribute(this.moid, "GracePeriodLeft")).intValue();
         String formattedTime = this.convertSec2HHMMSS(seconds);
         return formattedTime + " left on grace period";
      } else {
         return "";
      }
   }

   private String convertSec2HHMMSS(int second) {
      int hour = 0;
      int minute = 0;
      if(second > 60) {
         minute = second / 60;
         second %= 60;
      }

      if(minute > 60) {
         hour = minute / 60;
         minute %= 60;
      }

      String hourStr = this.to2Digits(hour);
      String minuteStr = this.to2Digits(minute);
      String secondStr = this.to2Digits(second);
      return hourStr + ":" + minuteStr + ":" + secondStr;
   }

   private String to2Digits(int digit) {
      return digit < 10?"0" + String.valueOf(digit):String.valueOf(digit);
   }

   public AttRef getLicenseFileTable() {
      MOID licenseFile = new MOID("com.ericsson.mlcn.im.LicenseFileTable", "0", this.resource.getRID());
      return new AttRef(licenseFile);
   }

   public String getFingerPrint() {
      try {
         String e = (String)this.resourceGetAttribute(this.moid, "FingerPrint");
         if(e != null) {
            byte[] bytes = e.getBytes();
            Hashtable mapper = new Hashtable();
            mapper.put(new BITS_BitSet(new byte[]{(byte)0}), new Integer(0));
            Integer v = (Integer)mapper.get(new BITS_BitSet(bytes));
            if(v != null && v.intValue() == 0) {
               StringBuffer zeroString = new StringBuffer();

               for(int i = 0; i < bytes.length; ++i) {
                  zeroString.append("0");
               }

               return zeroString.toString();
            }

            if(this.isStringAlphaNumeric(e)) {
               return e;
            }
         }
      } catch (OperationException var7) {
         Logger.getLogger().debug("Failed to read FingerPrint.", var7);
      }

      return "";
   }

   private boolean isStringAlphaNumeric(String s) {
      return s.length() == 0?true:Pattern.matches("[\\p{Alnum}\\s]+", s);
   }
}
