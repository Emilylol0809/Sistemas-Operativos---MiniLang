// for, modulo e interprete
int suma = 0;
for (int i = 0; i <= 10; i = i + 2) {
    suma = suma + i;
}
print(suma);

int divisibles = 0;
for (int n = 1; n <= 20; n = n + 1) {
    if (n % 3 == 0) {
        divisibles = divisibles + 1;
    }
}
print(divisibles);
