package tanvd.grazi.ide.language.rust

import com.intellij.psi.PsiElement
import org.rust.ide.injected.findDoctestInjectableRanges
import org.rust.lang.core.psi.RsDocCommentImpl
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.kex.ifTrue

class RsDocSupport : LanguageSupport() {
    companion object {
        //TODO-tanvd@undin Poor solution, but for better need support of Rust team
        private val checker = GrammarChecker(GrammarChecker.ignoringQuotes,
                ignoreChar = linkedSetOf({ _, cur -> cur == '/' }, { _, cur -> cur == '\"' }),
                replaceChar = linkedSetOf({ prev, cur ->
                    (cur == '#' && !prev.endsWith('#')).ifTrue { '\n' }
                }))
    }

    override fun isRelevant(element: PsiElement) = element is RsDocCommentImpl

    override fun check(element: PsiElement): Set<Typo> {
        require(element is RsDocCommentImpl) { "Got not RsDocCommentImpl in a RsDocSupport" }
        val ranges = findDoctestInjectableRanges(element).flatten().toList()
        return checker.check(setOf(element),
                indexBasedIgnore = { _, index -> ranges.any { it.contains(index) } })
    }
}
