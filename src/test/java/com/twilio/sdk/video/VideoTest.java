package com.twilio.sdk.video;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import com.twilio.http.TwilioRestClient;
import com.twilio.jwt.accesstoken.AccessToken;
import com.twilio.jwt.accesstoken.VideoGrant;
import com.twilio.rest.video.v1.RoomCreator;
import com.twilio.rest.video.v1.RoomUpdater;
import com.twilio.sdk.video.Room.State;
import com.twilio.sdk.video.loader.NativeLoader;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.twilio.rest.video.v1.Room.RoomType.GROUP;
import static com.twilio.rest.video.v1.Room.RoomType.PEER_TO_PEER;
import static com.twilio.rest.video.v1.Room.RoomStatus.COMPLETED;
import static com.twilio.rest.video.v1.Room.RoomStatus.IN_PROGRESS;
import static com.twilio.rest.video.v1.Room.VideoCodec.VP8;
import static com.twilio.rest.video.v1.Room.VideoCodec.H264;

public class VideoTest {
    public static final String TWILIO_PROD_ENV = "prod";
    public static final String TWILIO_STAGE_ENV = "stage";
    public static final String TWILIO_DEV_ENV = "dev";

    public static final String TWILIO_ENV_NAME = "TWILIO_ENVIRONMENT";

    public static final String PROP_ACCOUNT_SID = "ACCOUNT_SID";
    public static final String PROP_API_KEY = "API_KEY";
    public static final String PROP_API_KEY_SECRET = "API_KEY_SECRET";
    public static final String PROP_ROOM_NAME = "ROOM_NAME";
    public static final String PROP_MEDIA_REGION = "MEDIA_REGION";

    private static final String IDENTITY_PREFIX_ALICE = "alice-";
    private static final String IDENTITY_PREFIX_BOB = "bob-";

    private static final Logger LOG = LoggerFactory.getLogger(VideoTest.class);

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
                LOG.info("FRAMES RECEIVED: {}", this.frameCount);
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
            LOG.info("Participant {} published audio track {}", participant.getIdentity(), publication.getTrackSid());
        }

        @Override
        public void onAudioTrackUnpublished(RemoteParticipant participant, RemoteAudioTrackPublication publication) {
            LOG.info("Participant {} unpublished audio track {}", participant.getIdentity(), publication.getTrackSid());
        }

        @Override
        public void onAudioTrackEnabled(RemoteParticipant participant, RemoteAudioTrackPublication publication) {
            LOG.info("Participant {} enabled audio track {}", participant.getIdentity(), publication.getTrackSid());
        }

        @Override
        public void onAudioTrackDisabled(RemoteParticipant participant, RemoteAudioTrackPublication publication) {
            LOG.info("Participant {} disabled audio track {}", participant.getIdentity(), publication.getTrackSid());
        }

        @Override
        public void onAudioTrackSubscribed(RemoteParticipant participant,
                RemoteAudioTrackPublication publication,
                RemoteAudioTrack track) {
            LOG.info("Subscribed to audio track {} of participant {}", publication.getTrackSid(), participant.getIdentity());
        }

        @Override
        public void onAudioTrackSubscriptionFailed(RemoteParticipant participant,
                RemoteAudioTrackPublication publication,
                TwilioError twilio_error) {
            LOG.info("Failed to subscribe to audio track {} of participant {}; error = {}, message = {}",
                    publication.getTrackSid(),
                    participant.getIdentity(),
                    twilio_error.getCode().swigValue(),
                    twilio_error.getMessage());
        }

        @Override
        public void onAudioTrackUnsubscribed(RemoteParticipant participant,
                RemoteAudioTrackPublication publication,
                RemoteAudioTrack track) {
            LOG.info("Unsubscribed from audio track {} of participant {}", publication.getTrackSid(), participant.getIdentity());
        }

        @Override
        public void onVideoTrackPublished(RemoteParticipant participant, RemoteVideoTrackPublication publication) {
            LOG.info("Participant {} published video track {}", participant.getIdentity(), publication.getTrackSid());
        }

        @Override
        public void onVideoTrackUnpublished(RemoteParticipant participant, RemoteVideoTrackPublication publication) {
            LOG.info("Participant {} unpublished video track {}", participant.getIdentity(), publication.getTrackSid());
        }

        @Override
        public void onVideoTrackEnabled(RemoteParticipant participant, RemoteVideoTrackPublication publication) {
            LOG.info("Participant {} enabled video track {}", participant.getIdentity(), publication.getTrackSid());
        }

        @Override
        public void onVideoTrackDisabled(RemoteParticipant participant, RemoteVideoTrackPublication publication) {
            LOG.info("Participant {} disabled video track {}", participant.getIdentity(), publication.getTrackSid());
        }

        @Override
        public void onVideoTrackSubscribed(RemoteParticipant participant,
                RemoteVideoTrackPublication publication,
                RemoteVideoTrack track) {
            LOG.info("Subscribed to video track {} of participant {}", publication.getTrackSid(), participant.getIdentity());

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
            LOG.info("Failed to subscribe to video track {} of participant {}; error = {}, message = {}",
                    publication.getTrackSid(),
                    participant.getIdentity(),
                    twilio_error.getCode().swigValue(),
                    twilio_error.getMessage());
        }

        @Override
        public void onVideoTrackUnsubscribed(RemoteParticipant participant,
                RemoteVideoTrackPublication publication,
                RemoteVideoTrack track) {
            LOG.info("Unsubscribed from video track {} of participant {}", publication.getTrackSid(), participant.getIdentity());

            track.getWebRtcTrack().RemoveSink(this.videoTrackObservers.get(publication.getTrackSid()));
        }

        @Override
        public void onDataTrackPublished(RemoteParticipant participant, RemoteDataTrackPublication publication) {
            LOG.info("Participant {} published data track {}", participant.getIdentity(), publication.getTrackSid());
        }

        @Override
        public void onDataTrackUnpublished(RemoteParticipant participant, RemoteDataTrackPublication publication) {
            LOG.info("Participant {} unpublished data track {}", participant.getIdentity(), publication.getTrackSid());
        }

        @Override
        public void onDataTrackSubscribed(RemoteParticipant participant,
                RemoteDataTrackPublication publication,
                RemoteDataTrack track) {
            LOG.info("Subscribed to data track {} of participant {}", publication.getTrackSid(), participant.getIdentity());
        }

        @Override
        public void onDataTrackSubscriptionFailed(RemoteParticipant participant,
                RemoteDataTrackPublication publication,
                TwilioError twilio_error) {
            LOG.info("Failed to subscribe to data track {} of participant {}; error = {}, message = {}",
                    publication.getTrackSid(),
                    participant.getIdentity(),
                    twilio_error.getCode().swigValue(),
                    twilio_error.getMessage());
        }

        @Override
        public void onDataTrackUnsubscribed(RemoteParticipant participant,
                RemoteDataTrackPublication publication,
                RemoteDataTrack track) {
            LOG.info("Unsubscribed from data track {} of participant {}", publication.getTrackSid(), participant.getIdentity());
        }
    }

    private static class TestRoomObserver extends RoomObserver {
        final private RemoteParticipantObserver observer;
        final private CompletableFuture<Room> connected;
        final private CompletableFuture<Void> disconnected;

        volatile private boolean isConnected;

        public TestRoomObserver(final RemoteParticipantObserver observer) {
            this.observer = observer;
            this.connected = new CompletableFuture<>();
            this.disconnected = new CompletableFuture<>();

            this.isConnected = false;
        }

        public boolean isConnected() {
            return this.isConnected;
        }

        public Room waitForRoomConnect(final long time, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            LOG.info("Waiting for room to become connected ...");
            return this.connected.get(time, unit);
        }

        public void waitForRoomDisconnect(final long time, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            LOG.info("Waiting for room to become disconnected ...");
            this.disconnected.get(time, unit);
        }

        @Override
        public void onConnected(Room room) {
            LOG.info("Connected to room {} with SID {}", room.getName(), room.getSid());
            LOG.info("Number of participants in the room: {}", room.getRemoteParticipants().size());

            this.isConnected = true;
            this.connected.complete(room);

            final List<String> identities = room.getRemoteParticipants().keys();
            for (final String identity: identities) {
                LOG.info("Adding observer for participant {}", identity);

                final RemoteParticipant participant = room.getRemoteParticipants().get(identity);
                participant.setObserver(this.observer);
            }
        }

        @Override
        public void onDisconnected(Room room, TwilioError error) {
            LOG.info("Room {} disconnected (with{} error)", room.getName(), error == null ? "out" : "");
            
            this.isConnected = false;

            if (error != null) {
                LOG.info("Error code = {}, message = {}", error.getCode().swigValue(), error.getMessage());

                this.disconnected.completeExceptionally(new RuntimeException(error.getMessage()));
            } else {
                this.disconnected.complete(null);
            }
        }

        @Override
        public void onConnectFailure(Room room, TwilioError error) {
            LOG.info("Failed to connect to room {}, error code = {}", room.getName(), error.getCode().swigValue());

            this.connected.completeExceptionally(new RuntimeException(error.getMessage()));
        }

        @Override
        public void onParticipantConnected(Room room, RemoteParticipant participant) {
            LOG.info("Particpant {} connected to room {}, adding observer ...", participant.getIdentity(), room.getName());

            participant.setObserver(this.observer);
        }

        @Override
        public void onParticipantDisconnected(Room room, RemoteParticipant participant) {
            LOG.info("Particpant {} disconnected from room {}, removing observer ...", participant.getIdentity(), room.getName());

            participant.setObserver(null);
        }

        @Override
        public void onRecordingStarted(Room room) {
            LOG.info("Recording started for room {}", room.getName());
        }

        @Override
        public void onRecordingStopped(Room room) {
            LOG.info("Recording stopped for room {}", room.getName());
        }
    }

    private String account;
    private String apiKey;
    private String apiKeySecret;
    private String environment;
    private String mediaRegion;

    private String room;

    private String identityAlice;
    private String tokenAlice;

    private String identityBob;
    private String tokenBob;

    private TestRemoteParticipantObserver remoteParticipantObserver;
    private TestRoomObserver roomObserverAlice;
    private TestRoomObserver roomObserverBob;

    private MediaFactory mfAlice = null;
    private MediaFactory mfBob = null;

    private LocalAudioTrack audioTrackAlice = null;
    private LocalVideoTrack videoTrackAlice = null;

    private LocalAudioTrack audioTrackBob = null;
    private LocalVideoTrack videoTrackBob = null;

    @Before
    public void setup() {
        this.account = System.getProperty(PROP_ACCOUNT_SID);
        this.apiKey = System.getProperty(PROP_API_KEY);
        this.apiKeySecret = System.getProperty(PROP_API_KEY_SECRET);
        this.room = System.getProperty(PROP_ROOM_NAME);

        final String env = System.getenv(TWILIO_ENV_NAME);
        if (env != null && !env.isEmpty()) {
            if (env.equalsIgnoreCase("staging") || env.equalsIgnoreCase("stage")) {
                this.environment = TWILIO_STAGE_ENV;
            } else if (env.equalsIgnoreCase("development") || env.equalsIgnoreCase("dev")) {
                this.environment = TWILIO_DEV_ENV;
            } else {
                this.environment = TWILIO_PROD_ENV;
            }
        } else {
            this.environment = TWILIO_PROD_ENV;
        }

        final String mediaRegion = System.getProperty(PROP_MEDIA_REGION);
        if (mediaRegion != null && !mediaRegion.isEmpty()) {
            try {
                this.mediaRegion = MediaRegion.valueOf(mediaRegion).name().toLowerCase();
            } catch (Exception e) {
                System.out.println(String.format("Media region %s is invalid, using default value ...", mediaRegion));
                this.mediaRegion = MediaRegion.US1.name().toLowerCase();
            }
        } else {
            this.mediaRegion = MediaRegion.US1.name().toLowerCase();
        }

        if (this.room == null || this.room.isEmpty()) {
            this.room = "room-" + UUID.randomUUID().toString();
        }

        LOG.info("Account SID:    {}", this.account);
        LOG.info("API Key:        {}", this.apiKey);
        LOG.info("API Key Secret: {}", this.apiKeySecret);
        LOG.info("Media Region:   {}", this.mediaRegion);
        LOG.info("Room Name:      {}", this.room);

        this.remoteParticipantObserver = new TestRemoteParticipantObserver();
    }

    @After
    public void teardown() {
        this.roomObserverAlice = null;
        this.roomObserverBob = null;

        this.audioTrackBob = null;
        this.videoTrackBob = null;
        this.audioTrackAlice = null;
        this.videoTrackBob = null;

        this.mfBob = null;
        this.mfAlice = null;
    }

    @Test
    public void testCodecVectors() {
        setupIdentityAlice();
        this.mfAlice = MediaFactory.create(new MediaOptions());
        final ConnectOptions connectOptions1 = getConnectOptions(this.tokenAlice, this.room, this.mfAlice, null, null, null);
        final List<AudioCodec> audioCodecs = connectOptions1.getPreferredAudioCodecs();
        assertTrue(audioCodecs.isEmpty());
        final ConnectOptions connectOptions2 = getConnectOptions(this.tokenAlice,
                this.room,
                this.mfAlice,
                ImmutableList.of(new H264Codec(), new Vp8Codec(), new Vp9Codec()),
                null,
                null);
        final List<VideoCodec> videoCodecs = connectOptions2.getPreferredVideoCodecs();
        assertFalse(videoCodecs.isEmpty());
        assertEquals(videoCodecs.size(), 3);
        assertEquals(videoCodecs.get(0).getName(), "H264");
        assertEquals(videoCodecs.get(1).getName(), "VP8");
        assertEquals(videoCodecs.get(2).getName(), "VP9");
    }

    @Test
    public void testOneParticipantConnectToP2PRoom() throws InterruptedException, ExecutionException, TimeoutException {
        if (checkForAccountInfo()) {
            assertTrue(createRoom(this.room, null, null, PEER_TO_PEER, null));
            setupIdentityAlice();
            final Room roomAlice = connectAlice(null);
            assertTrue(this.roomObserverAlice.isConnected());
            assertEquals(roomAlice.getState(), State.kConnected);
            Thread.sleep(13000);
            roomAlice.disconnect();
            this.roomObserverAlice.waitForRoomDisconnect(10, TimeUnit.SECONDS);
            assertFalse(this.roomObserverAlice.isConnected());
            assertEquals(roomAlice.getState(), State.kDisconnected);
            assertTrue(completeRoom(this.room));
        }
    }

    @Test
    public void testTwoParticipantConnectToP2PRoom() throws InterruptedException, ExecutionException, TimeoutException {
        if (checkForAccountInfo()) {
            assertTrue(createRoom(this.room, null, null, PEER_TO_PEER, null));
            setupIdentityAlice();
            final Room roomAlice = connectAlice(null);
            assertTrue(this.roomObserverAlice.isConnected());
            assertEquals(roomAlice.getState(), State.kConnected);
            setupIdentityBob();
            final Room roomBob = connectBob(null);
            assertTrue(this.roomObserverBob.isConnected());
            assertEquals(roomBob.getState(), State.kConnected);
            Thread.sleep(13000);
            roomBob.disconnect();
            this.roomObserverBob.waitForRoomDisconnect(10, TimeUnit.SECONDS);
            assertFalse(this.roomObserverBob.isConnected());
            assertEquals(roomBob.getState(), State.kDisconnected);
            roomAlice.disconnect();
            this.roomObserverAlice.waitForRoomDisconnect(10, TimeUnit.SECONDS);
            assertFalse(this.roomObserverAlice.isConnected());
            assertEquals(roomAlice.getState(), State.kDisconnected);
            assertTrue(completeRoom(this.room));
        }
    }

    @Test
    public void testOneParticipantConnectToGroupRoom() throws InterruptedException, ExecutionException, TimeoutException {
        if (checkForAccountInfo()) {
            assertTrue(createRoom(this.room, this.mediaRegion, null, GROUP, Lists.newArrayList(VP8, H264)));
            setupIdentityAlice();
            final Room roomAlice = connectAlice(null);
            assertTrue(this.roomObserverAlice.isConnected());
            assertEquals(roomAlice.getState(), State.kConnected);
            Thread.sleep(13000);
            roomAlice.disconnect();
            this.roomObserverAlice.waitForRoomDisconnect(10, TimeUnit.SECONDS);
            assertFalse(this.roomObserverAlice.isConnected());
            assertEquals(roomAlice.getState(), State.kDisconnected);
            assertTrue(completeRoom(this.room));
        }
    }

    @Test
    public void testTwoParticipantsConnectToGroupRoom() throws InterruptedException, ExecutionException, TimeoutException {
        if (checkForAccountInfo()) {
            assertTrue(createRoom(this.room, this.mediaRegion, null, GROUP, Lists.newArrayList(VP8, H264)));
            setupIdentityAlice();
            final Room roomAlice = connectAlice(null);
            assertTrue(this.roomObserverAlice.isConnected());
            assertEquals(roomAlice.getState(), State.kConnected);
            setupIdentityBob();
            final Room roomBob = connectBob(null);
            assertTrue(this.roomObserverBob.isConnected());
            assertEquals(roomBob.getState(), State.kConnected);
            Thread.sleep(13000);
            roomBob.disconnect();
            this.roomObserverBob.waitForRoomDisconnect(10, TimeUnit.SECONDS);
            assertFalse(this.roomObserverBob.isConnected());
            assertEquals(roomBob.getState(), State.kDisconnected);
            roomAlice.disconnect();
            this.roomObserverAlice.waitForRoomDisconnect(10, TimeUnit.SECONDS);
            assertFalse(this.roomObserverAlice.isConnected());
            assertEquals(roomAlice.getState(), State.kDisconnected);
            assertTrue(completeRoom(this.room));
        }
    }

    @Test
    public void testTwoParticipantsConnectToGroupRoomWithUnsupportedCodec() throws InterruptedException, ExecutionException, TimeoutException {
        if (checkForAccountInfo()) {
            assertTrue(createRoom(this.room, this.mediaRegion, null, GROUP, Lists.newArrayList(H264)));
            setupIdentityAlice();
            final List<VideoCodec> preferredVCodecs = ImmutableList.of(new H264Codec());
            final Room roomAlice = connectAlice(preferredVCodecs);
            assertTrue(this.roomObserverAlice.isConnected());
            assertEquals(roomAlice.getState(), State.kConnected);
            setupIdentityBob();
            final Room roomBob = connectBob(preferredVCodecs);
            assertTrue(this.roomObserverBob.isConnected());
            assertEquals(roomBob.getState(), State.kConnected);
            Thread.sleep(13000);
            roomBob.disconnect();
            this.roomObserverBob.waitForRoomDisconnect(10, TimeUnit.SECONDS);
            assertFalse(this.roomObserverBob.isConnected());
            assertEquals(roomBob.getState(), State.kDisconnected);
            roomAlice.disconnect();
            this.roomObserverAlice.waitForRoomDisconnect(10, TimeUnit.SECONDS);
            assertFalse(this.roomObserverAlice.isConnected());
            assertEquals(roomAlice.getState(), State.kDisconnected);
            assertTrue(completeRoom(this.room));
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

    protected ConnectOptions getConnectOptions(final String token,
            final String room,
            final MediaFactory mf,
            final List<VideoCodec> preferredVCodecs,
            final List<LocalAudioTrack> audioTracks,
            final List<LocalVideoTrack> videoTracks) {
        assertNotNull(token);
        assertNotNull(room);
        assertNotNull(mf);

        final ConnectOptions.Builder builder = new ConnectOptions.Builder(token).setRoomName(room).setMediaFactory(mf);

        if (preferredVCodecs != null && !preferredVCodecs.isEmpty()) {
            builder.setPreferredVideoCodecs(preferredVCodecs);
        }

        if (audioTracks != null && !audioTracks.isEmpty()) {
            builder.setAudioTracks(audioTracks);
        }

        if (videoTracks != null && !videoTracks.isEmpty()) {
            builder.setVideoTracks(videoTracks);
        }

        return builder.build();
    }

    protected Room connectAlice(final List<VideoCodec> preferredVCodecs) throws InterruptedException, ExecutionException, TimeoutException {
        this.mfAlice = MediaFactory.create(new MediaOptions());
        assertNotNull("Alice's media factory object is valid", this.mfAlice);
        this.videoTrackAlice = this.mfAlice.createVideoTrack(true, MediaConstraints.defaultVideoConstraints());
        assertNotNull("Alice's video track object is valid", this.videoTrackAlice);
        this.audioTrackAlice = this.mfAlice.createAudioTrack(new AudioTrackOptions(true));
        assertNotNull("Alice's audio track object is valid", this.audioTrackAlice);

        LOG.info("Connecting Alice to the room {}", this.room);
        this.roomObserverAlice = new TestRoomObserver(this.remoteParticipantObserver);
        video.connect(getConnectOptions(this.tokenAlice,
                        this.room,
                        this.mfAlice,
                        preferredVCodecs,
                        Lists.newArrayList(this.audioTrackAlice),
                        Lists.newArrayList(this.videoTrackAlice)),
                this.roomObserverAlice);
        return this.roomObserverAlice.waitForRoomConnect(10, TimeUnit.SECONDS);
    }

    protected Room connectBob(final List<VideoCodec> preferredVCodecs) throws InterruptedException, ExecutionException, TimeoutException {
        this.mfBob = MediaFactory.create(new MediaOptions());
        assertNotNull("Bob's media factory object is valid", this.mfBob);
        this.videoTrackBob = this.mfBob.createVideoTrack(true, MediaConstraints.defaultVideoConstraints());
        assertNotNull("Bob's video track object is valid", this.videoTrackBob);
        this.audioTrackBob = this.mfBob.createAudioTrack(new AudioTrackOptions(true));
        assertNotNull("Bob's audio track object is valid", this.audioTrackBob);

        LOG.info("Connecting Bob to the room {}", this.room);
        this.roomObserverBob = new TestRoomObserver(this.remoteParticipantObserver);
        video.connect(getConnectOptions(this.tokenBob,
                        this.room,
                        mfBob,
                        preferredVCodecs,
                        Lists.newArrayList(this.audioTrackBob),
                        Lists.newArrayList(this.videoTrackBob)),
                this.roomObserverBob);
        return this.roomObserverBob.waitForRoomConnect(10, TimeUnit.SECONDS);
    }

    protected TwilioRestClient getTwilioRestClient() {
        final TwilioRestClient.Builder builder = new TwilioRestClient.Builder(this.apiKey, this.apiKeySecret)
                .accountSid(this.account);
        if (this.environment != null && !this.environment.isEmpty() && this.environment != TWILIO_PROD_ENV) {
            builder.region(this.environment);
        }
        return builder.build();
    }

    protected boolean createRoom(final String room,
            final String mediaRegion,
            final String statusCallback,
            final com.twilio.rest.video.v1.Room.RoomType roomType,
            final List<com.twilio.rest.video.v1.Room.VideoCodec> videoCodecs) {
        assertNotNull(room);
        assertNotNull(roomType);

        final com.twilio.rest.video.v1.RoomCreator roomCreator = new RoomCreator().setUniqueName(room).setType(roomType);

        if (mediaRegion != null && !mediaRegion.isEmpty()) {
            roomCreator.setMediaRegion(mediaRegion);
        }

        if (statusCallback != null && !statusCallback.isEmpty()) {
            roomCreator.setStatusCallback(statusCallback);
        }

        if (videoCodecs != null && !videoCodecs.isEmpty()) {
            roomCreator.setVideoCodecs(videoCodecs);
        }

        final com.twilio.rest.video.v1.Room restRoom = roomCreator.create(getTwilioRestClient());
        LOG.debug("CREATED ROOM: {}", restRoom.toString());
        return restRoom.getStatus() == IN_PROGRESS;
    }

    protected boolean completeRoom(final String room) {
        assertNotNull(room);

        final com.twilio.rest.video.v1.Room restRoom = new RoomUpdater(room, COMPLETED).update(getTwilioRestClient());
        LOG.debug("COMPLETED ROOM: {}", restRoom.toString());
        return restRoom.getStatus() == COMPLETED;
    }
}
