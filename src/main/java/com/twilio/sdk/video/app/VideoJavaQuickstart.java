package com.twilio.sdk.video.app;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.twilio.jwt.accesstoken.AccessToken;
import com.twilio.jwt.accesstoken.VideoGrant;

import com.twilio.sdk.video.loader.NativeLoader;
import com.twilio.sdk.video.AudioTrackOptions;
import com.twilio.sdk.video.ConnectOptions;
import com.twilio.sdk.video.H264Codec;
import com.twilio.sdk.video.LocalAudioTrack;
import com.twilio.sdk.video.LocalVideoTrack;
import com.twilio.sdk.video.MediaConstraints;
import com.twilio.sdk.video.MediaFactory;
import com.twilio.sdk.video.MediaOptions;
import com.twilio.sdk.video.RemoteAudioTrack;
import com.twilio.sdk.video.RemoteAudioTrackPublication;
import com.twilio.sdk.video.RemoteDataTrack;
import com.twilio.sdk.video.RemoteDataTrackPublication;
import com.twilio.sdk.video.RemoteParticipant;
import com.twilio.sdk.video.RemoteParticipantObserver;
import com.twilio.sdk.video.RemoteVideoTrack;
import com.twilio.sdk.video.RemoteVideoTrackPublication;
import com.twilio.sdk.video.Room;
import com.twilio.sdk.video.RoomObserver;
import com.twilio.sdk.video.TwilioError;
import com.twilio.sdk.video.VideoFrame;
import com.twilio.sdk.video.VideoSinkForVideoFrame;
import com.twilio.sdk.video.video;

public class VideoJavaQuickstart {
    static {
        NativeLoader.loadNativeLibraries();
    }

    private static class TestSinkForVideoFrame extends VideoSinkForVideoFrame {
        private int frameCount = 0;

        TestSinkForVideoFrame() {
            super();
        }

        public void OnFrame(VideoFrame videoFrame) {
            if (++this.frameCount % 30 == 0) {
                System.out.println("FRAMES RECEIVED: " + this.frameCount);
            }
        }
    }

    private static class TestRemoteParticipantObserver extends RemoteParticipantObserver {
        public TestRemoteParticipantObserver() {
            super();
        }

        @Override
        public void onAudioTrackPublished(RemoteParticipant participant, RemoteAudioTrackPublication publication) {
            System.out.println(String.format("Participant %s published audio track %s",
                    participant.getIdentity(),
                    publication.getTrackSid()));
        }

        @Override
        public void onAudioTrackUnpublished(RemoteParticipant participant, RemoteAudioTrackPublication publication) {
            System.out.println(String.format("Participant %s unpublished audio track %s",
                    participant.getIdentity(),
                    publication.getTrackSid()));
        }

        @Override
        public void onAudioTrackEnabled(RemoteParticipant participant, RemoteAudioTrackPublication publication) {
            System.out.println(String.format("Participant %s enabled audio track %s",
                    participant.getIdentity(),
                    publication.getTrackSid()));
        }

        @Override
        public void onAudioTrackDisabled(RemoteParticipant participant, RemoteAudioTrackPublication publication) {
            System.out.println(String.format("Participant %s disabled audio track %s",
                    participant.getIdentity(),
                    publication.getTrackSid()));
        }

        @Override
        public void onAudioTrackSubscribed(RemoteParticipant participant,
                RemoteAudioTrackPublication publication,
                RemoteAudioTrack track) {
            System.out.println(String.format("Subscribed to audio track %s of participant %s",
                    publication.getTrackSid(),
                    participant.getIdentity()));
        }

        @Override
        public void onAudioTrackSubscriptionFailed(RemoteParticipant participant,
                RemoteAudioTrackPublication publication,
                TwilioError twilio_error) {
            System.out.println(String.format("Failed to subscribe to audio track %s of participant %s; error = %d, message = %s",
                    publication.getTrackSid(),
                    participant.getIdentity(),
                    twilio_error.getCode().swigValue(),
                    twilio_error.getMessage()));
        }

        @Override
        public void onAudioTrackUnsubscribed(RemoteParticipant participant,
                RemoteAudioTrackPublication publication,
                RemoteAudioTrack track) {
            System.out.println(String.format("Unsubscribed from audio track %s of participant %s",
                    publication.getTrackSid(),
                    participant.getIdentity()));
        }

        @Override
        public void onVideoTrackPublished(RemoteParticipant participant, RemoteVideoTrackPublication publication) {
            System.out.println(String.format("Participant %s published video track %s",
                    participant.getIdentity(),
                    publication.getTrackSid()));
        }

        @Override
        public void onVideoTrackUnpublished(RemoteParticipant participant, RemoteVideoTrackPublication publication) {
            System.out.println(String.format("Participant %s unpublished video track %s",
                    participant.getIdentity(),
                    publication.getTrackSid()));
        }

        @Override
        public void onVideoTrackEnabled(RemoteParticipant participant, RemoteVideoTrackPublication publication) {
            System.out.println(String.format("Participant %s enabled video track %s",
                    participant.getIdentity(),
                    publication.getTrackSid()));
        }

        @Override
        public void onVideoTrackDisabled(RemoteParticipant participant, RemoteVideoTrackPublication publication) {
            System.out.println(String.format("Participant %s disabled video track %s",
                    participant.getIdentity(),
                    publication.getTrackSid()));
        }

        @Override
        public void onVideoTrackSubscribed(RemoteParticipant participant,
                RemoteVideoTrackPublication publication,
                RemoteVideoTrack track) {
            System.out.println(String.format("Subscribed to video track %s of participant %s",
                    publication.getTrackSid(),
                    participant.getIdentity()));
        }

        @Override
        public void onVideoTrackSubscriptionFailed(RemoteParticipant participant,
                RemoteVideoTrackPublication publication,
                TwilioError twilio_error) {
            System.out.println(String.format("Failed to subscribe to video track %s of participant %s; error = %d, message = %s",
                    publication.getTrackSid(),
                    participant.getIdentity(),
                    twilio_error.getCode().swigValue(),
                    twilio_error.getMessage()));
        }

        @Override
        public void onVideoTrackUnsubscribed(RemoteParticipant participant,
                RemoteVideoTrackPublication publication,
                RemoteVideoTrack track) {
            System.out.println(String.format("Unsubscribed from video track %s of participant %s",
                    publication.getTrackSid(),
                    participant.getIdentity()));
        }

        @Override
        public void onDataTrackPublished(RemoteParticipant participant, RemoteDataTrackPublication publication) {
            System.out.println(String.format("Participant %s published data track %s",
                    participant.getIdentity(),
                    publication.getTrackSid()));
        }

        @Override
        public void onDataTrackUnpublished(RemoteParticipant participant, RemoteDataTrackPublication publication) {
            System.out.println(String.format("Participant %s unpublished data track %s",
                    participant.getIdentity(),
                    publication.getTrackSid()));
        }

        @Override
        public void onDataTrackSubscribed(RemoteParticipant participant,
                RemoteDataTrackPublication publication,
                RemoteDataTrack track) {
            System.out.println(String.format("Subscribed to data track %s of participant %s",
                    publication.getTrackSid(),
                    participant.getIdentity()));
        }

        @Override
        public void onDataTrackSubscriptionFailed(RemoteParticipant participant,
                RemoteDataTrackPublication publication,
                TwilioError twilio_error) {
            System.out.println(String.format("Failed to subscribe to data track %s of participant %s; error = %d, message = %s",
                    publication.getTrackSid(),
                    participant.getIdentity(),
                    twilio_error.getCode().swigValue(),
                    twilio_error.getMessage()));
        }

        @Override
        public void onDataTrackUnsubscribed(RemoteParticipant participant,
                RemoteDataTrackPublication publication,
                RemoteDataTrack track) {
            System.out.println(String.format("Unsubscribed from data track %s of participant %s",
                    publication.getTrackSid(),
                    participant.getIdentity()));
        }        
    }

    private static class TestRoomObserver extends RoomObserver {
        private RemoteParticipantObserver observer;

        public TestRoomObserver(final RemoteParticipantObserver observer) {
            this.observer = observer;
        }

        @Override
        public void onConnected(Room room) {
            System.out.println(String.format("Connected to room: %s", room.getName()));
            System.out.println(String.format("PARTICIPANTS IN THE ROOM: %d", room.getRemoteParticipants().size()));
            final List<String> identities = room.getRemoteParticipants().keys();
            for (final String identity: identities) {
                System.out.println(String.format("Adding observer for participant %s", identity));
                final RemoteParticipant participant = room.getRemoteParticipants().get(identity);
                participant.setObserver(this.observer);
            }
        }

        @Override
        public void onDisconnected(Room room, TwilioError error) {
            System.out.println(String.format("Room %s disconnected (with%s error)", room.getName(), error == null ? "out" : ""));
            if (error != null) {
                System.out.println(String.format("Error code = %d, message = %s", error.getCode(), error.getMessage()));
            }
        }

        @Override
        public void onConnectFailure(Room room, TwilioError twilio_error) {
            System.out.println(String.format("Failed to connect to room %s, error code = %d",
                    room.getName(),
                    twilio_error.getCode().swigValue()));
        }

        @Override
        public void onParticipantConnected(Room room, RemoteParticipant participant) {
            System.out.println(String.format("Particpant %s connected to room %s, adding observer ...",
                    participant.getIdentity(),
                    room.getName()));
            participant.setObserver(this.observer);
        }

        @Override
        public void onParticipantDisconnected(Room room, RemoteParticipant participant) {
            System.out.println(String.format("Particpant %s disconnected from room %s, removing observer ...",
                    participant.getIdentity(),
                    room.getName()));
            participant.setObserver(null);
        }

        @Override
        public void onRecordingStarted(Room room) {
            System.out.println(String.format("Recording started for room %s", room.getName()));
        }

        @Override
        public void onRecordingStopped(Room room) {
            System.out.println(String.format("Recording stopped for room %s", room.getName()));
        }
    }

    private static String accountSid;
    private static String apiKey;
    private static String apiKeySecret;

    private static String roomName;
    private static String identity;
    private static String token;

    private static TestRemoteParticipantObserver remoteParticipantObserver;
    private static TestRoomObserver roomObserver;

    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println(String.format("Account SID:    %s", System.getProperty("ACCOUNT_SID")));
        System.out.println(String.format("API Key:        %s", System.getProperty("API_KEY")));
        System.out.println(String.format("API Key Secret: %s", System.getProperty("API_KEY_SECRET")));
        System.out.println(String.format("Room Name:      %s", System.getProperty("ROOM_NAME")));

        System.out.println("Press any key to begin ...");
        System.in.read();

        accountSid = System.getProperty("ACCOUNT_SID");
        apiKey = System.getProperty("API_KEY");
        apiKeySecret = System.getProperty("API_KEY_SECRET");
        roomName = System.getProperty("ROOM_NAME");

        if (roomName == null || roomName.isEmpty()) {
            roomName = "room-" + UUID.randomUUID().toString();
        }

        identity = "participant-" + UUID.randomUUID().toString();

        VideoGrant grant = new VideoGrant();
        token = new AccessToken.Builder(accountSid, apiKey, apiKeySecret)
                .identity(identity)
                .grant(grant).build().toJwt();

        remoteParticipantObserver = new TestRemoteParticipantObserver();
        roomObserver = new TestRoomObserver(remoteParticipantObserver);

        final MediaOptions mediaOptions = new MediaOptions();
        final MediaFactory mediaFactory = MediaFactory.create(mediaOptions);
        final LocalVideoTrack videoTrack = mediaFactory.createVideoTrack(false, MediaConstraints.defaultVideoConstraints());
        final LocalAudioTrack audioTrack = mediaFactory.createAudioTrack(new AudioTrackOptions(true));

        final ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .setRoomName(roomName)
                .setMediaFactory(mediaFactory)
                .setPreferredVideoCodecs(Lists.newArrayList(new H264Codec()))
                .setAudioTracks(Lists.newArrayList(audioTrack))
                .setVideoTracks(Lists.newArrayList(videoTrack)).build();

        final Room room = video.connect(connectOptions, roomObserver);
        Thread.sleep(10000);
        room.disconnect();
        Thread.sleep(5000);
    }
}
