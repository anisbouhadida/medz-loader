---
name: javadoc
description: 'Ensure that Java types are documented with Javadoc comments and follow best practices for documentation.'
---

# Java Documentation (Javadoc) Best Practices

- Public and protected members should be documented with Javadoc comments.
- Package-private and private members should be documented as well, especially if they are complex or not self-explanatory.
- The first sentence of the Javadoc comment is the summary description. It should be a concise overview of what the method does and end with a period.
- All Javadoc comments **must** use the Markdown style (`///` line comments), not the classic `/** */` block style.
- Use backticks for inline code snippets: `` `MyClass` `` or `` `someMethod()` ``.
- Use fenced code blocks (` ``` `) for multi-line code examples.
- Use `[ClassName]` or `[ClassName#methodName(ParamType)]` for cross-references to types and members (replaces `{@link}`).
- Use `[text][ClassName#method()]` for a labelled cross-reference link.
- Use standard Markdown lists (`-`) for enumerations inside doc comments.
- Do **not** use HTML tags (`<p>`, `<pre>`, `<code>`, etc.) or classic inline tags (`{@link}`, `{@code}`, `{@inheritDoc}`) inside `///` comments — use Markdown equivalents instead.
- Use `@param` for method parameters. The description starts with a lowercase letter and does not end with a period.
- Use `@return` for method return values. The description starts with a lowercase letter and does not end with a period.
- Use `@throws` to document exceptions thrown by methods.
- Use `@see` for additional references to other types or members.
- Use `@param <T>` for type parameters in generic types or methods.
- Use `@since` to indicate when the feature was introduced (e.g., version number).
- Use `@version` to specify the version of the member.
- Use `@author` to specify the author of the code.
- Use `@deprecated` to mark a member as deprecated and provide an alternative. Always pair with the `@Deprecated` annotation.
- For inherited documentation, add a short `///` comment only when there is a meaningful behavioural difference; otherwise omit the comment entirely (the compiler inherits the parent doc automatically).
