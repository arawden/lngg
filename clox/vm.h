#ifndef clox_vm_h
#define clox_vm_h

#include "chunk.h"
#include "table.h"
#include "value.h"

#define STACK_MAX 256

typedef struct {
    Chunk *chunk;
    uint8_t *ip;  // instruction pointer
    Value stack[STACK_MAX];
    Value *stackTop;  // points where next value will be pushed to
    Table globals;
    Table strings;  // interned strings
    Obj *objects;   // pointer to head of list of allocated objects
} VM;

typedef enum {
    INTERPRET_OK,
    INTERPRET_COMPILE_ERROR,
    INTERPRET_RUNTIME_ERROR
} InterpretResult;

extern VM vm;

void initVM();
void freeVM();

InterpretResult interpret(const char *source);

// stack
void push(Value value);
Value pop();

#endif
