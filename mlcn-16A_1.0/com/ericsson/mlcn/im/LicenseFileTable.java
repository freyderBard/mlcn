package com.ericsson.mlcn.im;

import com.ericsson.mlne.im.common.MrBean;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import no.axxessit.im.common.ByteArray;
import no.axxessit.mgt.MBeanInfo;

public class LicenseFileTable extends MrBean {

   private static final long serialVersionUID = -5950170286744595153L;


   public LicenseFileTable(MBeanInfo info) {
      super(info);
   }

   public String getFileStatus() {
      ByteArray attribute = (ByteArray)this.resource.getAttribute(this.moid, "FileStatus");
      if(attribute == null) {
         return "";
      } else {
         String status = "OK";
         if(attribute.get(7)) {
            status = "Unkown Format";
         } else if(attribute.get(6)) {
            status = "Unkown Signature Type";
         } else if(attribute.get(5)) {
            status = "Unkown Finger Print Method";
         } else if(attribute.get(4)) {
            status = "Unkown Finger Print";
         }

         return status;
      }
   }

   public String getFileIntallationDateRO() {
      return this.getConvertedTime("FileIntallationDate");
   }

   public String getFileGenerationDateRO() {
      return this.getConvertedTime("FileGenerationDate");
   }

   private List getTime(Date time) {
      LinkedList timeStamp = new LinkedList();
      timeStamp.add(Integer.valueOf(time.getYear() + 1900));
      timeStamp.add(Integer.valueOf(time.getMonth()));
      timeStamp.add(Integer.valueOf(time.getDate()));
      timeStamp.add(Integer.valueOf(time.getHours()));
      timeStamp.add(Integer.valueOf(time.getMinutes()));
      timeStamp.add(Integer.valueOf(time.getSeconds()));
      return timeStamp;
   }

   private String getConvertedTime(String attr) {
      Date time = (Date)this.resource.getAttribute(this.moid, attr);
      if(time == null) {
         return "";
      } else {
         List timeStamp = this.getTime(time);
         Calendar c = Calendar.getInstance();
         c.set(1, ((Integer)timeStamp.get(0)).intValue());
         c.set(2, ((Integer)timeStamp.get(1)).intValue());
         c.set(5, ((Integer)timeStamp.get(2)).intValue());
         c.set(11, ((Integer)timeStamp.get(3)).intValue());
         c.set(12, ((Integer)timeStamp.get(4)).intValue());
         c.set(13, ((Integer)timeStamp.get(5)).intValue());
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
         String timeString = sdf.format(c.getTime());
         return timeString;
      }
   }
}
