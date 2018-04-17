#ifndef _TWILIO_VIDEO_CAPTURE_H_
#define _TWILIO_VIDEO_CAPTURE_H_

#include <atomic>
#include <memory>

#include "webrtc/base/thread.h"
#include "webrtc/base/asyncinvoker.h"
#include "webrtc/media/base/videocapturer.h"
#include "webrtc/modules/video_coding/timing.h"

namespace twilio {
namespace media {
namespace capture {

class FakeVideoCapturer: public cricket::VideoCapturer, public rtc::Runnable {
public:
    FakeVideoCapturer();
    ~FakeVideoCapturer();

    void Run(rtc::Thread *thread) override;

    cricket::CaptureState Start(const cricket::VideoFormat &capture_format) override;
    void Stop() override;
    bool IsRunning() override;
    bool IsScreencast() const override;

protected:
    bool GetPreferredFourccs(std::vector<uint32_t> *fourccs) override;

private:
    const static int kFrameRate = 30;

    void generateFrame();

    std::unique_ptr<rtc::Thread> capture_thread_;
    std::unique_ptr<rtc::AsyncInvoker> invoker_;
    sigslot::signal1<FakeVideoCapturer *> signalDestroyed;
    std::atomic<bool> started_;
    int64_t timestamp_;
};

class VideoCapturerFactory {
public:
    static cricket::VideoCapturer *CreateFakeVideoCapturer();
    static cricket::VideoCapturer *CreateVideoCapturer();
};

} // namespace capture
} // namespace media
} // namespace twilio

#endif /* !_TWILIO_VIDEO_CAPTURE_H_ */

