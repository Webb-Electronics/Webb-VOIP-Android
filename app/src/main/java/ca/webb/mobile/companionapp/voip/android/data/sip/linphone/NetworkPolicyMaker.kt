package ca.webb.mobile.companionapp.voip.android.data.sip.linphone

import ca.webb.mobile.companionapp.voip.android.data.config.DeploymentConfig
import org.linphone.core.Core
import org.linphone.core.NatPolicy

/**
 * Network policy maker
 *
 * @constructor Create NO constructor
 */

class NetworkPolicyMaker {
    companion object {
        /**
         * Create policy for the domain to use for linphone
         *
         * The per-domain STUN servers come from [DeploymentConfig]; a domain
         * without a configured STUN server gets no NAT policy.
         *
         * @param core the core
         * @param domain the domain
         * @return the NAT policy to use if domain is found
         */
        fun createPolicy(core: Core, domain: String): NatPolicy? {
            val stunServer = DeploymentConfig.natStunServers[domain] ?: return null
            val policy = core.createNatPolicy()
            policy.stunServer = stunServer
            policy.isIceEnabled = true
            policy.isStunEnabled = true
            return policy
        }
    }
}
