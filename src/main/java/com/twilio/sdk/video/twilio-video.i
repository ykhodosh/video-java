%module(directors="1") video

%insert("runtime") %{
#define SWIG_JAVA_ATTACH_CURRENT_THREAD_AS_DAEMON
%}

%{
#include "webrtc/base/scoped_ref_ptr.h"
#include "webrtc/base/thread.h"
#include "webrtc/api/jsep.h"
#include "webrtc/media/base/videocapturer.h"
#include "webrtc/api/mediastreaminterface.h"
#include "webrtc/api/mediaconstraintsinterface.h"
#include "webrtc/modules/audio_device/include/audio_device.h"
#include "webrtc/api/peerconnectioninterface.h"

#include "twilio-video-capture.h"

#include "media/codec.h"
#include "media/stats.h"
#include "media/track.h"
#include "media/ice_options.h"
#include "media/media_stats.h"
#include "media/track_observer.h"
#include "media/media_constraints.h"
#include "media/data_track_options.h"
#include "media/audio_track_options.h"
#include "media/video_track_options.h"
#include "media/media_factory.h"
#include "video/connect_options.h"
#include "video/participant.h"
#include "video/local_participant.h"
#include "video/remote_participant.h"
#include "video/twilio_error.h"
#include "video/local_participant_observer.h"
#include "video/remote_participant_observer.h"
#include "video/room.h"
#include "video/room_observer.h"
#include "video/stats_report.h"
#include "video/stats_observer.h"
#include "video/video.h"

using namespace webrtc;
using namespace twilio;
using namespace twilio::media;
using namespace twilio::video;

namespace twilio {
namespace video {
enum class MediaRegion {
    US1,
    US2,
    IE1,
    DE1,
    IN1,
    BR1,
    SG1,
    JP1,
    AU1
};

struct PointerHolder {
    void *pointer_;
    PointerHolder(void *pointer) {
        pointer_ = pointer;
    }
};

twilio::video::Room *connect(twilio::video::ConnectOptions options, std::shared_ptr<twilio::video::RoomObserver> observer) {
    std::weak_ptr<twilio::video::RoomObserver> weak_observer(observer);
    return twilio::video::connect(options, weak_observer).release();
}
}
}
%}

// methods and fields related to webrtc::MediaConstraintsInterface
%ignore webrtc::MediaConstraintsInterface::Constraints::FindFirst(const std::string& key, std::string* value) const;
%ignore webrtc::MediaConstraintsInterface::kInternalConstraintPrefix;
%ignore webrtc::FindConstraint(const MediaConstraintsInterface* constraints, const std::string& key, bool* value, size_t* mandatory_constraints);
%ignore webrtc::FindConstraint(const MediaConstraintsInterface* constraints, const std::string& key, int* value, size_t* mandatory_constraints);
%ignore webrtc::CopyConstraintsIntoRtcConfiguration(const MediaConstraintsInterface* constraints, PeerConnectionInterface::RTCConfiguration* configuration);
%ignore webrtc::CopyConstraintsIntoAudioOptions(const MediaConstraintsInterface* constraints, cricket::AudioOptions* options);

// ignore whole webrtc::MediaStreamInterface
%ignore webrtc::MediaStreamInterface;

// do not expose webrtc::AudioSourceInterface and webrtc::AudioProcessorInterface
%ignore webrtc::AudioTrackInterface::GetSource() const;
%ignore webrtc::AudioTrackInterface::GetAudioProcessor();
%ignore webrtc::AudioTrackInterface::GetSignalLevel(int* level);
%ignore webrtc::AudioSourceInterface;
%ignore webrtc::AudioProcessorInterface;

// same with webrtc::VideoTrackSourceInterface
%ignore webrtc::VideoTrackInterface::GetSource() const;
%ignore webrtc::VideoTrackSourceInterface;

// ditto webrtc::MediaSourceInterface
%ignore webrtc::MediaSourceInterface;

// disable webrtc::VideoFrame creation from Java side
%ignore webrtc::VideoFrame::VideoFrame(const rtc::scoped_refptr<VideoFrameBuffer>& buffer, webrtc::VideoRotation rotation, int64_t timestamp_us);
%ignore webrtc::VideoFrame::VideoFrame(const rtc::scoped_refptr<VideoFrameBuffer>& buffer, uint32_t timestamp, int64_t render_time_ms, VideoRotation rotation);
%ignore webrtc::VideoFrame::VideoFrame(const VideoFrame&);
%ignore webrtc::VideoFrame::VideoFrame(VideoFrame&);

// ignore twilio::video::connect(), which returns std::unique_ptr; replace with raw pointer
%ignore twilio::video::connect(ConnectOptions connect_options, std::weak_ptr<RoomObserver> room_observer);

// same for twilio::video::LocalParticipant::setObserver(), which takes std::weak_ptr; replace with std::shared_ptr
%ignore twilio::video::LocalParticipant::setObserver(std::weak_ptr<LocalParticipantObserver> observer);

// same for twilio::media::RemoteDataTrack::setObserver(), which takes std::weak_ptr; replace with std::shared_ptr
%ignore twilio::media::RemoteDataTrack::setObserver(std::weak_ptr<RemoteDataTrackObserver> observer);

// ditto for twilio::video::RemoteParticipant::setObserver(), which takes std::weak_ptr; replace with std::shared_ptr
%ignore twilio::video::RemoteParticipant::setObserver(std::weak_ptr<RemoteParticipantObserver> observer);

// and finally for twilio::video::Room::getStats()
%ignore twilio::video::Room::getStats(std::weak_ptr<StatsObserver> observer);

// ignore notifier queue/thread in twilio::video::ConnectOptions
%ignore twilio::video::ConnectOptions::getNotifierQueue() const;
%ignore twilio::video::ConnectOptions::getNotifierThread() const;
%ignore twilio::video::ConnectOptions::Builder::setNotifierQueue(dispatch_queue_t queue);
%ignore twilio::video::ConnectOptions::Builder::setNotifierThread(rtc::Thread* thread);

// ignore twilio::media::IceOptions::RTCIceServers and twilio::media::IceServer::RTCIceServer
%ignore twilio::media::IceOptions::RTCIceServers(const IceServers& iceServers);
%ignore twilio::media::IceServer::RTCIceServer(const IceServer& server);

// ignore cricket::WebRtcVideoDecoderFactory and cricket::WebRtcVideoEncoderFactory in twilio::media::MediaOptions;
// also ignore threads and device module
%ignore twilio::media::MediaOptions::video_decoder_factory;
%ignore twilio::media::MediaOptions::video_encoder_factory;
%ignore twilio::media::MediaOptions::worker_thread;
%ignore twilio::media::MediaOptions::signaling_thread;
%ignore twilio::media::MediaOptions::networking_thread;
%ignore twilio::media::MediaOptions::audio_device_module;

// narrow down the API for twilio::media::MediaFactory
%ignore twilio::media::MediaFactory::createAudioSource(const cricket::AudioOptions& options = cricket::AudioOptions(),
                                                       bool register_recording_sink = false);
%ignore twilio::media::MediaFactory::createAudioTrack(rtc::scoped_refptr<webrtc::AudioSourceInterface> source,
                                                      const AudioTrackOptions& options = AudioTrackOptions());
%ignore twilio::media::MediaFactory::createVideoSource(cricket::VideoCapturer *external_capturer,
                                                       const MediaConstraints *constraints = nullptr);
%ignore twilio::media::MediaFactory::createVideoSource(const MediaConstraints *constraints = nullptr);
%ignore twilio::media::MediaFactory::createVideoTrack(rtc::scoped_refptr<webrtc::VideoTrackSourceInterface> source,
                                                      const VideoTrackOptions& options = VideoTrackOptions());
%ignore twilio::media::MediaFactory::createVideoTrack(const VideoTrackOptions& options = VideoTrackOptions());
%ignore twilio::media::MediaFactory::getAudioDeviceModule() const;
%ignore twilio::media::MediaFactory::getWorkerThread() const;
%ignore twilio::media::MediaFactory::getSignalingThread() const;

// replace automatically generated accessors for IceServer::urls with custom ones
%ignore twilio::media::IceServer::urls;

// do not generate setters for the following fields
%immutable twilio::video::StatsReport::peer_connection_id;
%immutable twilio::video::StatsReport::local_audio_track_stats;
%immutable twilio::video::StatsReport::local_video_track_stats;
%immutable twilio::video::StatsReport::remote_audio_track_stats;
%immutable twilio::video::StatsReport::remote_video_track_stats;
%immutable twilio::video::StatsReport::ice_candidate_pair_stats;
%immutable twilio::video::StatsReport::ice_candidate_stats;

%immutable twilio::media::IceCandidatePairStats::transport_id;
%immutable twilio::media::IceCandidatePairStats::local_candidate_id;
%immutable twilio::media::IceCandidatePairStats::remote_candidate_id;
%immutable twilio::media::IceCandidatePairStats::state;
%immutable twilio::media::IceCandidatePairStats::local_candidate_ip;
%immutable twilio::media::IceCandidatePairStats::remote_candidate_ip;
%immutable twilio::media::IceCandidatePairStats::priority;
%immutable twilio::media::IceCandidatePairStats::nominated;
%immutable twilio::media::IceCandidatePairStats::writable;
%immutable twilio::media::IceCandidatePairStats::readable;
%immutable twilio::media::IceCandidatePairStats::bytes_sent;
%immutable twilio::media::IceCandidatePairStats::bytes_received;
%immutable twilio::media::IceCandidatePairStats::total_round_trip_time;
%immutable twilio::media::IceCandidatePairStats::current_round_trip_time;
%immutable twilio::media::IceCandidatePairStats::available_outgoing_bitrate;
%immutable twilio::media::IceCandidatePairStats::available_incoming_bitrate;
%immutable twilio::media::IceCandidatePairStats::requests_received;
%immutable twilio::media::IceCandidatePairStats::requests_sent;
%immutable twilio::media::IceCandidatePairStats::responses_received;
%immutable twilio::media::IceCandidatePairStats::responses_sent;
%immutable twilio::media::IceCandidatePairStats::retransmissions_received;
%immutable twilio::media::IceCandidatePairStats::retransmissions_sent;
%immutable twilio::media::IceCandidatePairStats::consent_requests_received;
%immutable twilio::media::IceCandidatePairStats::consent_requests_sent;
%immutable twilio::media::IceCandidatePairStats::consent_responses_received;
%immutable twilio::media::IceCandidatePairStats::consent_responses_sent;
%immutable twilio::media::IceCandidatePairStats::active_candidate_pair;
%immutable twilio::media::IceCandidatePairStats::relay_protocol;

%immutable twilio::media::IceCandidateStats::transport_id;
%immutable twilio::media::IceCandidateStats::is_remote;
%immutable twilio::media::IceCandidateStats::ip;
%immutable twilio::media::IceCandidateStats::port;
%immutable twilio::media::IceCandidateStats::protocol;
%immutable twilio::media::IceCandidateStats::candidate_type;
%immutable twilio::media::IceCandidateStats::priority ;
%immutable twilio::media::IceCandidateStats::url;
%immutable twilio::media::IceCandidateStats::deleted;

%immutable twilio::media::BaseTrackStats::track_id;
%immutable twilio::media::BaseTrackStats::track_sid;
%immutable twilio::media::BaseTrackStats::packets_lost;
%immutable twilio::media::BaseTrackStats::codec;
%immutable twilio::media::BaseTrackStats::ssrc;
%immutable twilio::media::BaseTrackStats::timestamp;

%immutable twilio::media::LocalTrackStats::bytes_sent;
%immutable twilio::media::LocalTrackStats::packets_sent;
%immutable twilio::media::LocalTrackStats::round_trip_time;

%immutable twilio::media::RemoteTrackStats::bytes_received;
%immutable twilio::media::RemoteTrackStats::packets_received;

%immutable twilio::media::LocalAudioTrackStats::audio_level;
%immutable twilio::media::LocalAudioTrackStats::jitter;

%immutable twilio::media::VideoDimensions::width;
%immutable twilio::media::VideoDimensions::height;

%immutable twilio::media::LocalVideoTrackStats::capture_dimensions;
%immutable twilio::media::LocalVideoTrackStats::dimensions;
%immutable twilio::media::LocalVideoTrackStats::capture_frame_rate;
%immutable twilio::media::LocalVideoTrackStats::frame_rate;
%immutable twilio::media::LocalVideoTrackStats::initial_rtp_timestamp;
%immutable twilio::media::LocalVideoTrackStats::frames_encoded;

%immutable twilio::media::RemoteAudioTrackStats::audio_level;
%immutable twilio::media::RemoteAudioTrackStats::jitter;

%immutable twilio::media::RemoteVideoTrackStats::dimensions;
%immutable twilio::media::RemoteVideoTrackStats::frame_rate;

// the following classes never instantiated on Java side
%nodefault twilio::media::BaseTrackStats;
%nodefault twilio::media::RemoteTrackStats;
%nodefault twilio::media::IceCandidateStats;
%nodefault twilio::media::IceCandidatePairStats;
%nodefault twilio::media::LocalDataTrackStats;
%nodefault twilio::media::LocalAudioTrackStats;
%nodefault twilio::media::LocalVideoTrackStats;
%nodefault twilio::media::RemoteDataTrackStats;
%nodefault twilio::media::RemoteAudioTrackStats;
%nodefault twilio::media::RemoteVideoTrackStats;

// allow calling from C++ to Java for observer classes
%feature("director") twilio::video::RoomObserver;
%feature("director") twilio::video::StatsObserver;
%feature("director") twilio::media::RemoteDataTrackObserver;
%feature("director") twilio::video::LocalParticipantObserver;
%feature("director") twilio::video::RemoteParticipantObserver;
%feature("director") rtc::VideoSinkInterface<webrtc::VideoFrame>;

// use value wrappers for classes with no default/copy c-tor
%feature("valuewrapper") twilio::media::DataTrackOptions;
%feature("valuewrapper") twilio::video::TwilioError;

// custom std::vector wrappers
%include twilio-custom-vectors.i
%shared_ptr_vector_as_immutable_list(twilio::media::AudioCodec, AudioCodec)
%shared_ptr_vector_as_immutable_list(twilio::media::VideoCodec, VideoCodec)

%shared_ptr_vector_as_immutable_list(twilio::media::LocalDataTrack, LocalDataTrack)
%shared_ptr_vector_as_immutable_list(twilio::media::LocalAudioTrack, LocalAudioTrack)
%shared_ptr_vector_as_immutable_list(twilio::media::LocalVideoTrack, LocalVideoTrack)

%shared_ptr_vector_as_immutable_list(twilio::media::AudioTrackPublication, AudioTrackPublication)
%shared_ptr_vector_as_immutable_list(twilio::media::LocalAudioTrackPublication, LocalAudioTrackPublication)
%shared_ptr_vector_as_immutable_list(twilio::media::RemoteAudioTrackPublication, RemoteAudioTrackPublication)

%shared_ptr_vector_as_immutable_list(twilio::media::VideoTrackPublication, VideoTrackPublication)
%shared_ptr_vector_as_immutable_list(twilio::media::LocalVideoTrackPublication, LocalVideoTrackPublication)
%shared_ptr_vector_as_immutable_list(twilio::media::RemoteVideoTrackPublication, RemoteVideoTrackPublication)

%shared_ptr_vector_as_immutable_list(twilio::media::DataTrackPublication, DataTrackPublication)
%shared_ptr_vector_as_immutable_list(twilio::media::LocalDataTrackPublication, LocalDataTrackPublication)
%shared_ptr_vector_as_immutable_list(twilio::media::RemoteDataTrackPublication, RemoteDataTrackPublication)

// typemaps for various int types
%include "stdint.i"

// typemaps for STL std::string
%include "std_string.i"

// generate proper Java enums for C++ enums
%include "enums.swg"
%javaconst(1);

// wrap VideoFrameBuffer data in java.nio.ByteBuffer
%typemap(jni) const unsigned char* "jobject"
%typemap(jtype) const unsigned char* "java.nio.ByteBuffer"
%typemap(jstype) const unsigned char* "java.nio.ByteBuffer"

%typemap(out) const unsigned char* {
#ifdef __cplusplus
  $result = jenv->NewDirectByteBuffer($1, 1000);
#else
  $result = NewDirectByteBuffer(jenv, $1, 1000);
#endif
}

%typemap(javaout) const unsigned char * {
  return $jnicall;
}

// rtc::scoped_refptr instantiations
%include "rtc-scoped-refptr.i"
%template(VideoFrameBufferRef) rtc::scoped_refptr<webrtc::VideoFrameBuffer>;

// std::shared_ptr
%include "std_shared_ptr.i"
%shared_ptr(twilio::media::AudioCodec)
%shared_ptr(twilio::media::G722Codec)
%shared_ptr(twilio::media::IsacCodec)
%shared_ptr(twilio::media::OpusCodec)
%shared_ptr(twilio::media::PcmaCodec)
%shared_ptr(twilio::media::PcmuCodec)
%shared_ptr(twilio::media::VideoCodec)
%shared_ptr(twilio::media::H264Codec)
%shared_ptr(twilio::media::Vp8Codec)
%shared_ptr(twilio::media::Vp9Codec)

%shared_ptr(twilio::media::Track)
%shared_ptr(twilio::media::DataTrack)
%shared_ptr(twilio::media::AudioTrack)
%shared_ptr(twilio::media::VideoTrack)
%shared_ptr(twilio::media::LocalDataTrack)
%shared_ptr(twilio::media::LocalAudioTrack)
%shared_ptr(twilio::media::LocalVideoTrack)
%shared_ptr(twilio::media::RemoteDataTrack)
%shared_ptr(twilio::media::RemoteAudioTrack)
%shared_ptr(twilio::media::RemoteVideoTrack)

%shared_ptr(twilio::media::MediaFactory)

%shared_ptr(twilio::video::RoomObserver)
%shared_ptr(twilio::video::StatsObserver)
%shared_ptr(twilio::media::RemoteDataTrackObserver)
%shared_ptr(twilio::video::LocalParticipantObserver)
%shared_ptr(twilio::video::RemoteParticipantObserver)

%shared_ptr(twilio::video::Participant)
%shared_ptr(twilio::video::LocalParticipant)
%shared_ptr(twilio::video::RemoteParticipant)

%shared_ptr(twilio::media::TrackPublication)
%shared_ptr(twilio::media::DataTrackPublication)
%shared_ptr(twilio::media::AudioTrackPublication)
%shared_ptr(twilio::media::VideoTrackPublication)
%shared_ptr(twilio::media::LocalDataTrackPublication)
%shared_ptr(twilio::media::LocalAudioTrackPublication)
%shared_ptr(twilio::media::LocalVideoTrackPublication)
%shared_ptr(twilio::media::RemoteDataTrackPublication)
%shared_ptr(twilio::media::RemoteAudioTrackPublication)
%shared_ptr(twilio::media::RemoteVideoTrackPublication)

// make sure we use the same std::shared_ptr types in director callbacks
%include "std-shared-ptr-director.i"
%director_shared_ptr(twilio::video::RemoteParticipant)

%director_shared_ptr(twilio::media::LocalDataTrack)
%director_shared_ptr(twilio::media::LocalAudioTrack)
%director_shared_ptr(twilio::media::LocalVideoTrack)
%director_shared_ptr(twilio::media::RemoteDataTrack)
%director_shared_ptr(twilio::media::RemoteAudioTrack)
%director_shared_ptr(twilio::media::RemoteVideoTrack)

%director_shared_ptr(twilio::media::LocalDataTrackPublication)
%director_shared_ptr(twilio::media::LocalAudioTrackPublication)
%director_shared_ptr(twilio::media::LocalVideoTrackPublication)
%director_shared_ptr(twilio::media::RemoteDataTrackPublication)
%director_shared_ptr(twilio::media::RemoteAudioTrackPublication)
%director_shared_ptr(twilio::media::RemoteVideoTrackPublication)

// rtc::VideoSinkInterface<webrtc::VideoFrame>
%include "webrtc/api/video/video_rotation.h"
%include "webrtc/api/video/video_frame_buffer.h"
%include "webrtc/api/video/video_frame.h"
%include "webrtc/media/base/videosinkinterface.h"
%include "webrtc/media/base/videosourceinterface.h"
%template(VideoSinkForVideoFrame) rtc::VideoSinkInterface<webrtc::VideoFrame>;

// vector templates
%include "std_vector.i"
namespace std {
%template(ConstraintsVector) vector<webrtc::MediaConstraintsInterface::Constraint>;

%template(IceCandidateStatsVector) vector<twilio::media::IceCandidateStats>;
%template(IceCandidatePairStatsVector) vector<twilio::media::IceCandidatePairStats>;
%template(LocalAudioTrackStatsVector) vector<twilio::media::LocalAudioTrackStats>;
%template(LocalVideoTrackStatsVector) vector<twilio::media::LocalVideoTrackStats>;
%template(RemoteAudioTrackStatsVector) vector<twilio::media::RemoteAudioTrackStats>;
%template(RemoteVideoTrackStatsVector) vector<twilio::media::RemoteVideoTrackStats>;

%template(StatsReportVector) vector<twilio::video::StatsReport>;

%template(IceServerVector) vector<twilio::media::IceServer>;
}

// map template for remote participants and custom extenstion
%include "std_map.i"
%extend std::map<std::string, std::shared_ptr<twilio::video::RemoteParticipant>> {
      std::vector<std::string> keys() {
          std::vector<std::string> keys;
          for (auto entry : *$self) {
              keys.push_back(entry.first);
          }
          return keys;
      }
}

namespace std {
%template(RemoteParticipantMap) std::map<std::string, std::shared_ptr<twilio::video::RemoteParticipant>>;
}

// std::unique_ptr
%include "std-unique-ptr.i"
%unique_ptr(twilio::video::TwilioError)

// handle byte array conversion in twilio::media::LocalDataTrack::send(const uint8_t* message, size_t size)
%include "various.i"
%apply (char *STRING, size_t LENGTH) { (const uint8_t* message, size_t size) }

// define rtc::Optional here to avoid pulling in a lot of crap
namespace rtc {
template <typename T> class Optional {
public:
    Optional();
    explicit Optional(const T& value);
    Optional(const Optional& m);
    ~Optional();

    //friend void swap(Optional& m1, Optional& m2);
    void reset();

    const T& value_or(const T& default_val) const;

    %extend {
        bool value_present() const {
            rtc::Optional<T> value = *$self;
            if (value) return true;
            return false;
        }

        const T& value() const {
            rtc::Optional<T> value = *$self;
            return *value;
        }

        T& value() {
            rtc::Optional<T> value = *$self;
            return *value;
        }
    }
};
}

// rtc::Optional instantiations
%template(IntOptional) rtc::Optional<int>;

// webrtc::MediaConstraintsInterface
%include "webrtc/api/mediaconstraintsinterface.h"

// webrtc::MediaStreamInterface, webrtc::AudioTrackInterface, ...
%include "webrtc/api/mediastreaminterface.h"

// twilio video interfaces
%include "media/codec.h"
%include "media/stats.h"
%include "media/track.h"
%include "media/ice_options.h"
%include "media/media_stats.h"
%include "media/track_observer.h"
%include "media/media_constraints.h"
%include "media/data_track_options.h"
%include "media/audio_track_options.h"
%include "media/video_track_options.h"
%include "media/media_factory.h"
%include "video/platform_info.h"
%include "video/connect_options.h"
%include "video/participant.h"
%include "video/local_participant.h"
%include "video/remote_participant.h"
%include "video/twilio_error.h"
%include "video/local_participant_observer.h"
%include "video/remote_participant_observer.h"
%include "video/room.h"
%include "video/room_observer.h"
%include "video/stats_observer.h"
%include "video/stats_report.h"
%include "video/video.h"

// extentions
namespace webrtc {
%extend MediaConstraintsInterface::Constraints {
    std::string FindFirst(const std::string& key) const {
        std::string value;
        if (!self->FindFirst(key, &value)) {
            return "";
        }
        return value;
    }
};
}

namespace twilio {
namespace media {
%extend IceServer {
    void setUrls(std::vector<std::string> urls) {
        $self->urls = urls;
    }

    std::vector<std::string> getUrls() {
        return $self->urls;
    }
};

%extend RemoteDataTrack {
    void setObserver(std::shared_ptr<twilio::media::RemoteDataTrackObserver> observer) {
        std::weak_ptr<twilio::media::RemoteDataTrackObserver> weak_observer(observer);
        self->setObserver(weak_observer);
    }
};

%extend MediaFactory {
    std::shared_ptr<twilio::media::LocalVideoTrack> createVideoTrack(bool use_fake_capturer, const twilio::media::VideoTrackOptions &options) {
        cricket::VideoCapturer *capturer;
        if (use_fake_capturer) {
            capturer = twilio::media::capture::VideoCapturerFactory::CreateFakeVideoCapturer();
        } else {
            capturer = twilio::media::capture::VideoCapturerFactory::CreateVideoCapturer();
        }

        return self->createVideoTrack(self->createVideoSource(capturer), options);
    }

    std::shared_ptr<twilio::media::LocalVideoTrack> createVideoTrack(bool use_fake_capturer, const MediaConstraints *constraints) {
        cricket::VideoCapturer *capturer;
        if (use_fake_capturer) {
            capturer = twilio::media::capture::VideoCapturerFactory::CreateFakeVideoCapturer();
        } else {
            capturer = twilio::media::capture::VideoCapturerFactory::CreateVideoCapturer();
        }

        return self->createVideoTrack(self->createVideoSource(capturer, constraints));
    }

    std::shared_ptr<twilio::media::LocalVideoTrack> createVideoTrack(bool use_fake_capturer, const twilio::media::VideoTrackOptions &options, const MediaConstraints *constraints) {
        cricket::VideoCapturer *capturer;
        if (use_fake_capturer) {
            capturer = twilio::media::capture::VideoCapturerFactory::CreateFakeVideoCapturer();
        } else {
            capturer = twilio::media::capture::VideoCapturerFactory::CreateVideoCapturer();
        }

        return self->createVideoTrack(self->createVideoSource(capturer, constraints), options);
    }
};
}

namespace video {
enum class MediaRegion {
    US1,
    US2,
    IE1,
    DE1,
    IN1,
    BR1,
    SG1,
    JP1,
    AU1
};

twilio::video::Room *connect(twilio::video::ConnectOptions options, std::shared_ptr<twilio::video::RoomObserver> observer);

%extend LocalParticipant {
    void setObserver(std::shared_ptr<twilio::video::LocalParticipantObserver> observer) {
        std::weak_ptr<twilio::video::LocalParticipantObserver> weak_observer(observer);
        self->setObserver(weak_observer);
    }
};

%extend RemoteParticipant {
    void setObserver(std::shared_ptr<twilio::video::RemoteParticipantObserver> observer) {
        std::weak_ptr<twilio::video::RemoteParticipantObserver> weak_observer(observer);
        self->setObserver(weak_observer);
    }
};

%extend Room {
    void getStats(std::shared_ptr<twilio::video::StatsObserver> observer) {
        std::weak_ptr<twilio::video::StatsObserver> weak_observer(observer);
        self->getStats(weak_observer);
    }
};
}
}
