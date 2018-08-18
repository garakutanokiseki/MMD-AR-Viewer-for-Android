LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := yuv420sp2rgb

LOCAL_SRC_FILES := \
	yuv420sp2rgb/yuv420sp2rgb.c

LOCAL_CFLAGS := -DANDROID_NDK \
                -DDISABLE_IMPORTGL
#ヘッダーの検索パス
LOCAL_C_INCLUDES := $(LOCAL_PATH)/yuv420sp2rgb

LOCAL_LDLIBS := -ldl -llog

include $(BUILD_SHARED_LIBRARY)
