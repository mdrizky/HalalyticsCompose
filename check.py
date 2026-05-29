with open("app/src/main/java/com/example/halalyticscompose/ui/screens/ManualInputScreen.kt", "r") as f:
    text = f.read()

def check_balance(text):
    stack = []
    for i, c in enumerate(text):
        if c in "({[":
            stack.append((c, i))
        elif c in ")}]":
            if not stack:
                print(f"Unmatched {c} at index {i}")
                return
            last_c, last_i = stack.pop()
            if (c == ')' and last_c != '(') or \
               (c == '}' and last_c != '{') or \
               (c == ']' and last_c != '['):
                print(f"Mismatched {c} at index {i}, expected match for {last_c} at {last_i}")
                return
    if stack:
        print(f"Unclosed {stack}")
    else:
        print("Perfect balance!")

check_balance(text)
