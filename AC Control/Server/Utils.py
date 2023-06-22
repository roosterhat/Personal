import re
from typing import Any, Callable
    
class Endpoint:
    pattern: re.Pattern
    operation: Callable[[], Any]
    def __init__(self, pattern: str | re.Pattern, operation: Callable[[], Any]):
        self.operation = operation
        if type(pattern) is str:
            self.pattern = re.compile(pattern)
        else:
            self.pattern = pattern