#include <fstream>
#include <stdio.h>

#include "glpk.h"

int main(int argc, char** argv) {
    if(argc < 3) {
        printf("USAGE: ./callGLPK [input filename] [output filename]");
        return -1;
    }

    char* filename_in = argv[1];
    char* filename_out = argv[2];

    std::ofstream lp_out;
    lp_out.open(filename_out);

    if(!lp_out.is_open()) {
        printf("could not open file lp_out.txt");
        return -1;
    }

    glp_prob* prob;
    
    prob = glp_create_prob();
    glp_read_lp(prob, NULL, filename_in);
    glp_simplex(prob, NULL);

    glp_print_sol(prob, "/tmp/lp_out_debug.txt");

    int num_cols = glp_get_num_cols(prob);
    for(int i = 1; i <= num_cols; i++) {
        const char* col_name = glp_get_col_name(prob, i);

        double val = glp_get_col_prim(prob, i);
        lp_out << col_name << " = " << val << "\n";
    }

    double obj_val = glp_get_obj_val(prob);
    lp_out << "objective value = " << obj_val << "\n";


    return 0;
}