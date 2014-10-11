/*
 * Copyright (C) 2013 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.glassware;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.services.mirror.model.Attachment;
import com.google.api.services.mirror.model.NotificationConfig;
import com.google.api.services.mirror.model.TimelineItem;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Handles POST requests from index.jsp
 *
 * @author Jenny Murphy - http://google.com/+JennyMurphy
 */
public class HeartyServlet extends HttpServlet {
    /**
     * Private class to process batch request results.
     * <p/>
     * For more information, see
     * https://code.google.com/p/google-api-java-client/wiki/Batch.
     */
    private final class BatchCallback extends JsonBatchCallback<TimelineItem> {
        private int success = 0;
        private int failure = 0;

        @Override
        public void onSuccess(TimelineItem item, HttpHeaders headers) throws IOException {
            ++success;
        }

        @Override
        public void onFailure(GoogleJsonError error, HttpHeaders headers) throws IOException {
            ++failure;
            LOG.info("Failed to insert item: " + error.getMessage());
        }
    }

    private static final Logger LOG = Logger.getLogger(HeartyServlet.class.getSimpleName());

    /**
     * Do stuff when buttons on index.jsp are clicked
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {

        String userId = AuthUtil.getUserId(req);
        Credential credential = AuthUtil.newAuthorizationCodeFlow().loadCredential(userId);
        String message = "";

        if (req.getParameter("operation").equals("insertHeartyData")) {
            LOG.fine("Inserting Hearty Timeline Item");

            String bundleId = String.valueOf(System.currentTimeMillis());

            TimelineItem timelineItem = new TimelineItem();
            Attachment bundleCover = new Attachment();

            String imgLoc = WebUtil.buildUrl(req, "/static/images/hearty_640x360.png");

            URL url = new URL(imgLoc);
            bundleCover.setContentType("image/png");

            timelineItem.setText("Hearty.io");
            timelineItem.setBundleId(bundleId);
            timelineItem.setIsBundleCover(true);

            TimelineItem timelineItemHeart = new TimelineItem();
            timelineItemHeart.setText("Heart Rate: " + FakeDatabase.getUserData(userId).heartRate);
            timelineItemHeart.setBundleId(bundleId);

            TimelineItem timelineItemSteps = new TimelineItem();
            timelineItemSteps.setText("Steps: " + FakeDatabase.getUserData(userId).stepCount);
            timelineItemSteps.setBundleId(bundleId);


            TimelineItem timelineItemActivity = new TimelineItem();
            timelineItemActivity.setText("Active minutes: "  + FakeDatabase.getUserData(userId).activeMinutes);
            timelineItemActivity.setBundleId(bundleId);

            // Triggers an audible tone when the timeline item is received
            timelineItem.setNotification(new NotificationConfig().setLevel("DEFAULT"));

            MirrorClient.insertTimelineItem(credential, timelineItem, "image/png", url.openStream());
            MirrorClient.insertTimelineItem(credential, timelineItemHeart);
            MirrorClient.insertTimelineItem(credential, timelineItemSteps);
            MirrorClient.insertTimelineItem(credential, timelineItemActivity);
            message = "A timeline item has been inserted.";

        } else {
            String operation = req.getParameter("operation");
            LOG.warning("Unknown operation specified " + operation);
            message = "I don't know how to do that";
        }
        WebUtil.setFlash(req, message);
        res.sendRedirect(WebUtil.buildUrl(req, "/hearty.jsp"));
    }
}
