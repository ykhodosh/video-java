#include "twilio-video-capture.h"

#include "webrtc/modules/video_capture/video_capture.h"
#include "webrtc/modules/video_capture/video_capture_factory.h"
#include "webrtc/media/engine/webrtcvideocapturerfactory.h"

#ifdef __APPLE__
#include "webrtc/sdk/objc/Framework/Classes/avfoundationvideocapturer.h"
#endif

namespace twilio {
namespace media {
namespace capture {

FakeVideoCapturer::FakeVideoCapturer(): started_(false), timestamp_(0) {
    SetId("FakeVideoCapturer");
}

FakeVideoCapturer::~FakeVideoCapturer() {
    signalDestroyed(this);
}

void FakeVideoCapturer::Run(rtc::Thread *thread) {
    invoker_.reset(new rtc::AsyncInvoker());
    invoker_->AsyncInvokeDelayed<void>(RTC_FROM_HERE, thread,
                                       rtc::Bind(&FakeVideoCapturer::generateFrame, this),
                                       0,
                                       0);
    while (!thread->IsQuitting()) {
        thread->ProcessMessages(rtc::ThreadManager::kForever);
    }

    thread->ProcessMessages(100);
    invoker_.reset();
}

cricket::CaptureState FakeVideoCapturer::Start(const cricket::VideoFormat &capture_format) {
    if (!started_) {
        SetCaptureFormat(&capture_format);
        capture_thread_.reset(new rtc::Thread());
        capture_thread_->SetName("FakeVideoCapturer", nullptr);
        started_ = capture_thread_->Start(this);
    }

    return started_ ? cricket::CS_RUNNING : cricket::CS_FAILED;
}

void FakeVideoCapturer::Stop() {
    if (started_) {
        capture_thread_->Stop();
        capture_thread_.reset();
        started_ = false;
    }
}

bool FakeVideoCapturer::IsRunning() {
    return started_;
}

bool FakeVideoCapturer::IsScreencast() const {
    return false;
}

bool FakeVideoCapturer::GetPreferredFourccs(std::vector<uint32_t> *fourccs) {
    fourccs->push_back(cricket::FOURCC_I420);
    return true;
}

void FakeVideoCapturer::generateFrame() {
    if (capture_thread_->IsQuitting() || !started_) {
        return;
    }
    int height = GetCaptureFormat()->height;
    int width = GetCaptureFormat()->width;

    if (timestamp_ == 0) {
        timestamp_ = (time(NULL)*rtc::kNumMicrosecsPerSec);
    } else {
        timestamp_ += rtc::kNumMicrosecsPerSec/kFrameRate;
    }

    rtc::scoped_refptr<webrtc::I420Buffer> buffer(webrtc::I420Buffer::Create(width, height));
    buffer->InitializeData();
    buffer->SetToBlack();
    OnFrame(webrtc::VideoFrame(buffer, webrtc::kVideoRotation_0, timestamp_), width, height);

    invoker_->AsyncInvokeDelayed<void>(RTC_FROM_HERE, capture_thread_.get(),
                                       rtc::Bind(&FakeVideoCapturer::generateFrame, this),
                                       33,
                                       0);
}

cricket::VideoCapturer *VideoCapturerFactory::CreateFakeVideoCapturer() {
    return new FakeVideoCapturer();
}

cricket::VideoCapturer *VideoCapturerFactory::CreateVideoCapturer() {
    cricket::VideoCapturer *capturer = nullptr;

#ifdef __APPLE__
    capturer = new webrtc::AVFoundationVideoCapturer();
#else
    std::unique_ptr<webrtc::VideoCaptureModule::DeviceInfo> vdi(webrtc::VideoCaptureFactory::CreateDeviceInfo());
    if (!vdi) {
        return new FakeVideoCapturer();
    } else {
        printf("Successfully instantiated video device info\n");
    }

    if (vdi->NumberOfDevices() == 0) {
        return new FakeVideoCapturer();
    } else {
        printf("Number of video devices: %d\n", vdi->NumberOfDevices());
    }

    const uint32_t kSize = 256;
    char name[kSize] = {0};
    char id[kSize] = {0};
    if (vdi->GetDeviceName(0, name, kSize, id, kSize) == -1) {
        return new FakeVideoCapturer();
    } else {
        printf("Using device %s with id = %s\n", name, id);
    }

    cricket::WebRtcVideoDeviceCapturerFactory factory;
    capturer = factory.Create(cricket::Device(name, 0));
    if (capturer == nullptr) {
        return new FakeVideoCapturer();
    } else {
        printf("Successfully created platform video capturer\n");
    }
#endif

    return capturer;
}

} // namespace capture
} // namespace media
} // namespace twilio
