import re

with open('app/src/main/java/com/example/ui/components/GoogleLoginDialog.kt', 'r') as f:
    content = f.read()

replacement = """onClick = { 
                                    isAuthenticatingGoogle = true
                                    performGoogleSignIn(
                                        context = context,
                                        coroutineScope = coroutineScope,
                                        onSuccess = { name, email ->
                                            ApiKeyManager.saveGoogleLogin(name, email)
                                            ApiKeyManager.markFirstLaunchPrompted()
                                            isAuthenticatingGoogle = false
                                            onOpenAvatarPicker()
                                        },
                                        onError = { error ->
                                            isAuthenticatingGoogle = false
                                            // Ideally show a toast or error message here
                                        }
                                    )
                                }"""

content = content.replace("onClick = { showGoogleAccountChooser = true }", replacement)

with open('app/src/main/java/com/example/ui/components/GoogleLoginDialog.kt', 'w') as f:
    f.write(content)
