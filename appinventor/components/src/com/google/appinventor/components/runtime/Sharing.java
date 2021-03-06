// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2020 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.content.Intent;
import android.net.Uri;

import android.support.v4.content.FileProvider;

import android.webkit.MimeTypeMap;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import java.io.File;


/**
 * Component for sharing files and/or messages through Android's built-in sharing
 * functionality.
 *
 * @author victsou@gmail.com (Victor Silva) - Picked up on @cfromknecht's work
 * and fixed file support.
 */
@DesignerComponent(version = YaVersion.SHARING_COMPONENT_VERSION,
    description ="Sharing is a non-visible component that enables sharing files and/or " +
        "messages between your app and other apps installed on a device. The component " +
        "will display a list of the installed apps that can handle the information provided, " +
        "and will allow the user to choose one to share the content with, for instance a " +
        "mail app, a social network app, a texting app, and so on.<br>" +
        "The file path can be taken directly from other components such as the Camera or the " +
        "ImagePicker, but can also be specified directly to read from storage. Be aware that " +
        "different devices treat storage differently, so a few things to try if, " +
        "for instance, you have a file called arrow.gif in the folder " +
        "<code>Appinventor/assets</code>, would be: <ul>" +
        "<li><code>\"file:///sdcard/Appinventor/assets/arrow.gif\"</code></li> or " +
        "<li><code>\"/storage/Appinventor/assets/arrow.gif\"</code></li></ul>",
    category = ComponentCategory.SOCIAL,
    nonVisible = true, iconName = "images/sharing.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.READ_EXTERNAL_STORAGE")
public class Sharing extends AndroidNonvisibleComponent {

  private String subject="";
  private String chooserTitle="";
  //  private YailList fileNames = new YailList();
  private String attachment="";
  private String toAddress="";
  private String ccAddress="";

  public Sharing(ComponentContainer container) {
    super(container.$form());
  }

  @SimpleProperty(description = "Subject to be shared")
  public void Subject(String subject) {
    this.subject = subject.trim();
  }

  @SimpleProperty
  public String Subject() {
    return this.subject;
  }

  @SimpleProperty(description = "Use this to pre-fill the send-to address. Separate email addresses using comma")
  public void EmailTo(String toAddress) {
    this.toAddress = toAddress.trim();
  }

  @SimpleProperty
  public String EmailTo() {
    return this.toAddress;
  }

  @SimpleProperty(description = "Use this to prefill the CC address. Separate email addresses using comma")
  public void EmailCC(String ccAddress) {
    this.ccAddress = ccAddress.trim();
  }

  @SimpleProperty
  public String EmailCC() {
    return this.ccAddress;
  }

  /**
   * Shares a message using Android' built-in sharing.
   */
  @SimpleFunction(description = "Shares a message through any capable " +
      "application installed on the phone by displaying a list of the available apps and " +
      "allowing the user to choose one from the list. The selected app will open with the " +
      "message inserted on it.")
  public void ShareMessage(String message) {
    Intent shareIntent = new Intent(Intent.ACTION_SEND);
    shareIntent.putExtra(Intent.EXTRA_TEXT, message);
        if (!Subject().equals("")) {
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, Subject());
        }

        if (!EmailTo().equals("")) {
            // for TO field, need to include in String array, otherwise won't work
            shareIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {EmailTo()});

        }
        if (!EmailCC().equals("")) {
            shareIntent.putExtra(Intent.EXTRA_CC, new String[] {EmailCC()});
        }

    shareIntent.setType("text/plain");

    // We cannot use Intent.createChooser(shareIntent, "Send using...") because it creates an
    // oversized pop up sharing window.
    this.form.startActivity(shareIntent);
  }

  /**
   * Shares a file using Android' built-in sharing.
   */
  @SimpleFunction(description = "Shares a file through any capable application "
      + "installed on the phone by displaying a list of the available apps and allowing the " +
      "user to choose one from the list. The selected app will open with the file inserted on it.")
  public void ShareFile(String file) {
    ShareFileWithMessage(file, "");
  }

  /**
   * Shares a file along with a message using Android' built-in sharing.
   */
  @SimpleFunction(description = "Shares both a file and a message through any capable application "
      + "installed on the phone by displaying a list of available apps and allowing the user to " +
      " choose one from the list. The selected app will open with the file and message inserted on it.")
  public void ShareFileWithMessage(String file, String message) {

    String packageName = form.$context().getPackageName();

    if (!file.startsWith("file://"))
      file = "file://" + file;

    Uri uri  = Uri.parse(file);
    File imageFile = new File(uri.getPath());
    if (imageFile.isFile()) {
      String fileExtension = file.substring(file.lastIndexOf(".")+1).toLowerCase();
      MimeTypeMap mime = MimeTypeMap.getSingleton();
      String type = mime.getMimeTypeFromExtension(fileExtension);

      Uri shareableUri = FileProvider.getUriForFile(form.$context(), packageName + ".provider", imageFile);
      Intent shareIntent = new Intent(Intent.ACTION_SEND);
      shareIntent.putExtra(Intent.EXTRA_STREAM, shareableUri);
      shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
      shareIntent.setType(type);
      if (message.length() > 0) {
        shareIntent.putExtra(Intent.EXTRA_TEXT, message);
      }

      if (!Subject().equals("")) {
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, Subject());
      }

      if (!EmailTo().equals("")) {
        // for TO field, need to include in String array, otherwise won't work
        shareIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {EmailTo()});

      }
      if (!EmailCC().equals("")) {
        shareIntent.putExtra(Intent.EXTRA_CC, new String[] {EmailCC()});
      }

      // We cannot use Intent.createChooser(shareIntent, "Send using...") because it creates an
      // oversized pop up sharing window.
      this.form.startActivity(shareIntent);
    }
    else {
      String eventName = "ShareFile";
      if (message.equals(""))
        eventName = "ShareFileWithMessage";
      form.dispatchErrorOccurredEvent(Sharing.this, eventName,
          ErrorMessages.ERROR_FILE_NOT_FOUND_FOR_SHARING, file);
    }
  }
}
