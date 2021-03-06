package org.soft2.kai.tensors.cpu

import org.junit.Before
import org.junit.Test
import org.soft2.kai.tensors.tensor
import org.soft2.kai.tensors.matrix
import org.soft2.kai.tensors.Kernel
import kotlin.test.assertEquals

class AdditionToScalar {

    @Before
    fun before() {
        Kernel.default = CpuKernel
    }


    @Test
    fun additionToInt() {

        val t = matrix("1 2 3", "4 5 6", "7 8 9")
        val e = matrix("3 4 5", "6 7 8", "9 10 11")

        assertEquals(e, t + 2)
    }

    @Test
    fun additionToFloat() {

        val t = matrix("1 2 3", "4 5 6", "7 8 9")
        val e = matrix("4.15 5.15 6.15", "7.15 8.15 9.15", "10.15 11.15 12.15")

        assertEquals(e, t + 3.15f)
    }

    @Test
    fun additionOnBatch() {
        val t = tensor(intArrayOf(10, 3, 3, 3)) { it.toFloat() / 2 }.shatter()
        val e = tensor(intArrayOf(10, 3, 3, 3)) { it.toFloat() / 2 + 3.15f }.shatter()

        assertEquals(e, t + 3.15f)
    }



}