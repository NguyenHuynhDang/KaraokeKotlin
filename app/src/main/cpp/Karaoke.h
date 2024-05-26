//
// Created by ADMIN on 5/20/2024.
//

#ifndef KARAOKEKOTLIN_KARAOKE_H
#define KARAOKEKOTLIN_KARAOKE_H

#include <OpenSource/SuperpoweredAndroidAudioIO.h>
#include <SuperpoweredEcho.h>
#include <SuperpoweredReverb.h>
#include <SuperpoweredAutomaticVocalPitchCorrection.h>
#include <memory>


class Karaoke {
private:
    bool isEffectEnable = false;
    bool isAutotuneEnable = false;
    float premicVol = .0f;
    float micVolume = .0f;
    SuperpoweredAndroidAudioIO *audioIO;
    Superpowered::Echo *echo;
    Superpowered::Reverb *reverb;
    Superpowered::AutomaticVocalPitchCorrection *autotune;
public:
    Karaoke(unsigned int sampleRate, unsigned int bufferSize);
    bool process(short int* output, unsigned int numFrames, unsigned int samplerate);
    void setEffectEnable(bool value);
    void setAutotuneEnable(bool value);
    void setEchoValue(int value);
    void setReverbValue(int value);
    void setMicVolume(float value);
    void stopRecord();
};

static Karaoke* karaoke = nullptr;

#endif
