package ca.webb.mobile.companionapp.voip.android.data.config

import ca.webb.mobile.companionapp.voip.android.BuildConfig

/**
 * Site-specific deployment settings, supplied at build time from the git-ignored
 * `deployment.properties` file at the project root (see
 * `deployment.properties.example`).
 *
 * Every setting is optional. When a setting is absent the corresponding
 * behaviour is simply switched off, so a checkout without a
 * `deployment.properties` still builds and runs -- users register against
 * whichever SIP server they enter on the Register screen.
 */
object DeploymentConfig {

    /**
     * Aliases that rewrite a SIP server address into its canonical domain, so
     * that the same account reached over the LAN and over the internet is
     * treated as one identity.
     */
    val domainAliases: Map<String, String> = parsePairs(BuildConfig.WEBB_DOMAIN_ALIASES)

    /**
     * STUN server to use per SIP domain. A domain absent from this map gets no
     * NAT policy at all.
     */
    val natStunServers: Map<String, String> = parsePairs(BuildConfig.WEBB_NAT_STUN_POLICIES)

    /**
     * Parse a comma-separated list of `key=value` pairs. Blank entries and
     * entries without a value are skipped so a partially filled configuration
     * cannot break start-up.
     *
     * @param raw the raw configuration string
     * @return the parsed pairs
     */
    private fun parsePairs(raw: String): Map<String, String> = raw.split(",")
        .mapNotNull { entry ->
            val key = entry.substringBefore('=', "").trim()
            val value = entry.substringAfter('=', "").trim()
            if (key.isEmpty() || value.isEmpty()) null else key to value
        }
        .toMap()
}
