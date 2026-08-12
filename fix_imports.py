with open('app/src/main/java/com/example/ui/components/GoogleLoginDialog.kt', 'r') as f:
    lines = f.readlines()

package_idx = -1
for i, line in enumerate(lines):
    if line.startswith("package com.example"):
        package_idx = i
        break

if package_idx > 0:
    # move the package declaration to the top
    package_line = lines.pop(package_idx)
    lines.insert(0, package_line)
    lines.insert(1, "\n")
    
    # ensure performGoogleSignIn is imported
    lines.insert(2, "import com.example.ui.components.performGoogleSignIn\n")

with open('app/src/main/java/com/example/ui/components/GoogleLoginDialog.kt', 'w') as f:
    f.writelines(lines)
