package org.soft2.kai.tensors.cpu

import org.soft2.kai.tensors.Handle
import org.soft2.kai.tensors.Kernel
import kotlin.math.max
import kotlin.math.sqrt


object CpuKernel: Kernel {

    override fun allocate(floatArray: FloatArray) = floatArray

    override fun size(h: Handle) = (h as FloatArray).size

    override fun toFloatArray(h: Handle) = h as FloatArray

    override fun release(h: Handle) {
    }

    override fun matrixMul(
        a: Handle, b: Handle,
        n: Int, m: Int, q: Int,
        beta: Float
    ): Handle {
        val ta = a as FloatArray
        val tb = b as FloatArray

        val ba = ta.size/n/m
        val bb = tb.size/m/q
        val bc = maxOf(ba,bb)

        val c = FloatArray(m*q*bc)
        val stripeA = if (ba > 1) n*m else 0
        val stripeB = if (bb > 1) m*q else 0

        for (h in 0 until bc) {
            for (i in 0 until n) {
                for (j in 0 until q) {
                    var s = 0f
                    for (k in 0 until m) {
                        val ea = ta[h * stripeA + i * n + k]
                        val eb = tb[h * stripeB + k * q + j]
                        s += ea*eb
                    }
                    c[h * n * q + i * q + j] = s
                }
            }
        }

        return c
    }

    override fun add(a: Handle, b: Handle, alpha: Float): Handle {
        val ta = a as FloatArray
        val tb = b as FloatArray

        val size = maxOf(a.size, b.size)
        val c = FloatArray(size)

        for (offset in 0 until size) {
            val ea = ta[offset % ta.size]
            val eb = tb[offset % tb.size]
            c[offset] = ea + eb*alpha
        }


        return c
    }

    override fun get(h: Handle, offset: Int) = (h as FloatArray)[offset]

    override fun transpose(h: Handle, n: Int, m: Int): FloatArray {
        val volume = n*m
        val A = h as FloatArray
        val B = FloatArray(A.size)
        val batch = B.size / volume

        for(l in 0 until batch) {
            for (i in 0 until n) {
                for (j in 0 until m) {
                    B[l*volume+j*n+i]  = A[l*volume+i*m+j]
                }
            }
        }
        return B
    }

    override fun update(h: Handle, alpha: Float, A: Handle) {
        val th = h as FloatArray
        val tA = A as FloatArray
        val volume = tA.size

        th.indices.forEach {i ->
            th[i] += alpha * tA[i % volume]
        }
    }

    override fun norm(a: Handle) = sqrt((bwMul(a,a) as FloatArray)[0])

    override fun bwMul(a: Handle, b: Handle): Handle {
        val fa = a as FloatArray
        val fb = b as FloatArray

        val size = max(fa.size, fb.size)
        var result = 0f

        for (i in 0 until size) {
            result += fa[i%fa.size] * fb[i%fb.size]
        }
        return result
    }

    override fun expand(a: Handle, volume: Int, targetVolume: Int): FloatArray {
        val fa = a as FloatArray
        val batch = fa.size / volume
        val fb = FloatArray(targetVolume*batch)
        val r = targetVolume/volume

        for( i in 0 until batch) {
            for (j in 0 until r) {
                fa.copyInto(fb, i * targetVolume+j*volume, i*volume, i*volume+volume)
            }
        }
        return fb
    }


    override fun toString() = "CPU"
}