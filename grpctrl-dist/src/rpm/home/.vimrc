
set autoindent
set backspace=indent,eol,start
set background=dark
set cindent
set cinkeys=0{,0},0),!^F,o,O,e
set encoding=utf8
set expandtab
set fileformat=unix
set hidden
set hlsearch
set langmenu=none
set mousehide
set nobackup
set nocompatible
set noerrorbells
set novisualbell
set number
set ruler
set scrolloff=10
set shiftwidth=4
set smartcase
set tabstop=4
set title
set viminfo='10,\"100,:20,%,n~/.viminfo
set wildmenu

syntax on
colorscheme darkblue

nnoremap ` '
nnoremap ' `

au BufReadPost * if line("'\"") | exec "normal '\"" | endif

