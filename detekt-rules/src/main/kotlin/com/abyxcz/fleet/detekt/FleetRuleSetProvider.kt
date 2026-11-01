package com.abyxcz.fleet.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

/** The `fleet` rule set, configured under `fleet:` in config/detekt/detekt.yml. */
class FleetRuleSetProvider : RuleSetProvider {
    override val ruleSetId: String = "fleet"

    override fun instance(config: Config): RuleSet =
        RuleSet(ruleSetId, listOf(ForbiddenNotNullAssertion(config)))
}
