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

import com.google.common.base.Strings;
import com.twilio.jwt.accesstoken.AccessToken;
import com.twilio.jwt.accesstoken.VideoGrant;

import com.twilio.sdk.video.loader.NativeLoader;

import static com.google.common.base.Strings.isNullOrEmpty;

public class VideoTest {
    private static final String IDENTITY_PREFIX_ALICE = "alice-";
    private static final String IDENTITY_PREFIX_BOB = "bob-";

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
        private ConcurrentMap<String, VideoSinkForVideoFrame> videoTrackObservers;

        public TestRemoteParticipantObserver() {
            super();
            this.videoTrackObservers = new ConcurrentHashMap<>();
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

            final VideoSinkForVideoFrame videoTrackObserver = new TestSinkForVideoFrame();
            this.videoTrackObservers.put(publication.getTrackSid(), videoTrackObserver);

            final VideoSinkWants videoSinkWants = new VideoSinkWants();
            videoSinkWants.setBlack_frames(true);
            videoSinkWants.setRotation_applied(true);

            track.getWebRtcTrack().AddOrUpdateSink(videoTrackObserver, videoSinkWants);
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

            track.getWebRtcTrack().RemoveSink(this.videoTrackObservers.get(publication.getTrackSid()));
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

    private String account;
    private String apiKey;
    private String apiKeySecret;

    private String roomName;

    private String identityAlice;
    private String tokenAlice;

    private String identityBob;
    private String tokenBob;

    private TestRemoteParticipantObserver remoteParticipantObserver;
    private TestRoomObserver roomObserver;

    private MediaFactory mediaFactoryAlice = null;
    private MediaFactory mediaFactoryBob = null;

    private LocalAudioTrack audioTrackAlice = null;
    private LocalVideoTrack videoTrackAlice = null;

    private LocalAudioTrack audioTrackBob = null;
    private LocalVideoTrack videoTrackBob = null;

    @Before
    public void setup() {
        System.out.println(String.format("Account SID:    %s", System.getProperty("ACCOUNT_SID")));
        System.out.println(String.format("API Key:        %s", System.getProperty("API_KEY")));
        System.out.println(String.format("API Key Secret: %s", System.getProperty("API_KEY_SECRET")));
        System.out.println(String.format("Room Name:      %s", System.getProperty("ROOM_NAME")));

        this.account = System.getProperty("ACCOUNT_SID");
        this.apiKey = System.getProperty("API_KEY");
        this.apiKeySecret = System.getProperty("API_KEY_SECRET");
        this.roomName = System.getProperty("ROOM_NAME");

        if (this.roomName == null || this.roomName.isEmpty()) {
            this.roomName = "room-" + UUID.randomUUID().toString();
        }

        this.remoteParticipantObserver = new TestRemoteParticipantObserver();
        this.roomObserver = new TestRoomObserver(this.remoteParticipantObserver);
    }

    @After
    public void teardown() {
        this.audioTrackBob = null;
        this.videoTrackBob = null;
        this.audioTrackAlice = null;
        this.videoTrackBob = null;
        this.mediaFactoryBob = null;
        this.mediaFactoryAlice = null;
    }

    @Test
    public void testOneParticipantConnect() throws InterruptedException {
        if (checkForAccountInfo()) {
            setupIdentityAlice();
            final Room room = connectAlice();
            Thread.sleep(23000);
            room.disconnect();
            Thread.sleep(23000);
        }
    }

    @Test
    public void testTwoParticipantConnect() throws InterruptedException {
        if (checkForAccountInfo()) {
            setupIdentityAlice();
            final Room roomAlice = connectAlice();
            Thread.sleep(23000);
            setupIdentityBob();
            final Room roomBob = connectBob();
            Thread.sleep(23000);
            roomBob.disconnect();
            Thread.sleep(23000);
            roomAlice.disconnect();
            Thread.sleep(23000);
        }
    }

    protected boolean checkForAccountInfo() {
        if (isNullOrEmpty(this.account) || isNullOrEmpty(this.apiKey) || isNullOrEmpty(this.apiKeySecret)) {
            System.out.println("Account information is not configured, cannot generate access token!");

            return false;
        } else {
            return true;
        }
    }

    protected void setupIdentityAlice() {
        this.identityAlice = IDENTITY_PREFIX_ALICE + UUID.randomUUID().toString();

        VideoGrant grant = new VideoGrant();
        this.tokenAlice = new AccessToken.Builder(this.account, this.apiKey, this.apiKeySecret)
                .identity(this.identityAlice)
                .grant(grant).build().toJwt();
    }

    protected void setupIdentityBob() {
        this.identityBob = IDENTITY_PREFIX_BOB + UUID.randomUUID().toString();

        VideoGrant grant = new VideoGrant();
        this.tokenBob = new AccessToken.Builder(this.account, this.apiKey, this.apiKeySecret)
                .identity(this.identityBob)
                .grant(grant).build().toJwt();
    }

    protected Room connectAlice() {
        this.mediaFactoryAlice = MediaFactory.create(new MediaOptions());
        this.videoTrackAlice = this.mediaFactoryAlice.createVideoTrack(false, MediaConstraints.defaultVideoConstraints());
        this.audioTrackAlice = this.mediaFactoryAlice.createAudioTrack(new AudioTrackOptions(true));

        final LocalAudioTrackVector audioTracks = new LocalAudioTrackVector();
        audioTracks.add(this.audioTrackAlice);

        final LocalVideoTrackVector videoTracks = new LocalVideoTrackVector();
        videoTracks.add(this.videoTrackAlice);

        final ConnectOptions connectOptions = new ConnectOptions.Builder(this.tokenAlice)
                .setRoomName(this.roomName)
                .setMediaFactory(this.mediaFactoryAlice)
                .setAudioTracks(audioTracks)
                .setVideoTracks(videoTracks).build();

        return video.connect(connectOptions, this.roomObserver);
    }

    protected Room connectBob() {
        this.mediaFactoryBob = MediaFactory.create(new MediaOptions());
        this.videoTrackBob = this.mediaFactoryBob.createVideoTrack(false, MediaConstraints.defaultVideoConstraints());
        this.audioTrackBob = this.mediaFactoryBob.createAudioTrack(new AudioTrackOptions(true));

        final LocalAudioTrackVector audioTracks = new LocalAudioTrackVector();
        audioTracks.add(this.audioTrackBob);

        final LocalVideoTrackVector videoTracks = new LocalVideoTrackVector();
        videoTracks.add(this.videoTrackBob);

        final ConnectOptions connectOptions = new ConnectOptions.Builder(this.tokenBob)
                .setRoomName(this.roomName)
                .setMediaFactory(this.mediaFactoryBob)
                .setAudioTracks(audioTracks)
                .setVideoTracks(videoTracks).build();

        return video.connect(connectOptions, this.roomObserver);
    }
}
