package com.twilio.sdk.video;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.twilio.jwt.accesstoken.AccessToken;
import com.twilio.jwt.accesstoken.VideoGrant;

import com.twilio.sdk.video.loader.NativeLoader;

public class VideoTest {

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
            final StringVector identities = room.getRemoteParticipants().keys();
            for (int index = 0; index < identities.size(); index++) {
                System.out.println(String.format("Adding observer for participant %s", identities.get(index)));
                final RemoteParticipant participant = room.getRemoteParticipants().get(identities.get(index));
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

    private String accountSid;
    private String apiKey;
    private String apiKeySecret;

    private String roomName;
    private String identity;
    private String token;

    private TestRemoteParticipantObserver remoteParticipantObserver;
    private TestRoomObserver roomObserver;

    @Before
    public void setup() {
        System.out.println(String.format("Account SID:    %s", System.getProperty("ACCOUNT_SID")));
        System.out.println(String.format("API Key:        %s", System.getProperty("API_KEY")));
        System.out.println(String.format("API Key Secret: %s", System.getProperty("API_KEY_SECRET")));
        System.out.println(String.format("Room Name:      %s", System.getProperty("ROOM_NAME")));

        this.accountSid = System.getProperty("ACCOUNT_SID");
        this.apiKey = System.getProperty("API_KEY");
        this.apiKeySecret = System.getProperty("API_KEY_SECRET");
        this.roomName = System.getProperty("ROOM_NAME");

        if (this.roomName == null || this.roomName.isEmpty()) {
            this.roomName = "room-" + UUID.randomUUID().toString();
        }

        this.identity = "participant-" + UUID.randomUUID().toString();

        VideoGrant grant = new VideoGrant();
        this.token = new AccessToken.Builder(this.accountSid, this.apiKey, this.apiKeySecret)
                .identity(this.identity)
                .grant(grant).build().toJwt();

        this.remoteParticipantObserver = new TestRemoteParticipantObserver();
        this.roomObserver = new TestRoomObserver(this.remoteParticipantObserver);
    }

    @Test
    public void testSingleParticipantConnect() throws InterruptedException {
        final MediaOptions mediaOptions = new MediaOptions();
        final MediaFactory mediaFactory = MediaFactory.create(mediaOptions);
        final LocalVideoTrack videoTrack = mediaFactory.createVideoTrack(false, MediaConstraints.defaultVideoConstraints());
        final LocalAudioTrack audioTrack = mediaFactory.createAudioTrack(new AudioTrackOptions(true));

        final LocalAudioTrackVector audioTracks = new LocalAudioTrackVector();
        audioTracks.add(audioTrack);

        final LocalVideoTrackVector videoTracks = new LocalVideoTrackVector();
        videoTracks.add(videoTrack);

        final ConnectOptions connectOptions = new ConnectOptions.Builder(this.token)
                .setRoomName(this.roomName)
                .setMediaFactory(mediaFactory)
                .setAudioTracks(audioTracks)
                .setVideoTracks(videoTracks).build();

        final Room room = video.connect(connectOptions, this.roomObserver);
        Thread.sleep(10000);
        room.disconnect();
        Thread.sleep(5000);
    }
}
