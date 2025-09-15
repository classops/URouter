package io.github.classops.urouter.plugin.transform.v8

object RouterAnnotation {

    const val ROUTER_PACKAGE = "io/github/classops/urouter/generated/route"
//    const val ROUTER_PACKAGE = "io/github/classops/urouter/route"

    fun isRouterPackage(parent: String): Boolean {
        return parent.endsWith(ROUTER_PACKAGE)
    }

}