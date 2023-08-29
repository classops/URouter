package io.github.classops.urouter.demo.module_a.service

import io.github.classops.urouter.service.IService

/**
 * 文件名：TestService <br/>
 * 描述：测试Service
 *
 * @author wangmingshuo
 * @since 2023/04/07 17:18
 */
interface TestService : IService {

    fun log()

    fun toast(text: String)
}