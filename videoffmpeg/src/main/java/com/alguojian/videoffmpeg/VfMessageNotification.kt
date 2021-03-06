package com.alguojian.videoffmpeg

import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor

/**
 * @author alguojian
 * @date 2019-02-21
 *
 * 使用rxJava进行消息的传递，可以跨组件
 */
class VfMessageNotification private constructor() {
    private val BUS = PublishProcessor.create<Any>().toSerialized()

    fun send(o: Any) {
        BUS.onNext(o)
    }

    fun toFlowable(): Flowable<Any> {
        return BUS
    }

    fun hasSubscribers(): Boolean {
        return BUS.hasSubscribers()
    }

    fun <T> toFlowable(classToSubscribe: Class<T>): Flowable<T> {
        return BUS
            .filter { classToSubscribe.isInstance(it) }
            .cast(classToSubscribe)
    }

    companion object {

        @Volatile
        private var sDefaultInstance: VfMessageNotification? = null

        @JvmStatic
        fun INSTANCE(): VfMessageNotification {
            if (sDefaultInstance == null) {
                synchronized(VfMessageNotification::class.java) {
                    if (sDefaultInstance == null) {
                        sDefaultInstance = VfMessageNotification()
                    }
                }
            }
            return sDefaultInstance!!
        }
    }
}