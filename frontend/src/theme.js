import { createTheme, responsiveFontSizes } from '@mui/material/styles';

const theme = createTheme({
  palette: {
    primary: {
      main: '#00A887', // Verde Turquesa (Arcos)
      contrastText: '#ffffff',
    },
    secondary: {
      main: '#E40046', // Fucsia (Quiosco)
      contrastText: '#ffffff',
    },
    info: {
      main: '#009FE3', // Azul (Iglesia)
      contrastText: '#ffffff',
    },
    background: {
      default: '#f8fafc', // Gris muy claro para el fondo de la app
      paper: '#ffffff',
    },
    text: {
      primary: '#1e293b', // Gris carbón
      secondary: '#64748b', // Gris medio
    },
  },
  typography: {
    fontFamily: [
      'Outfit',
      'Inter',
      'system-ui',
      '-apple-system',
      'BlinkMacSystemFont',
      '"Segoe UI"',
      'Roboto',
      'sans-serif',
    ].join(','),
    h1: {
      fontWeight: 800,
    },
    h2: {
      fontWeight: 700,
    },
    h3: {
      fontWeight: 700,
    },
    h4: {
      fontWeight: 700,
    },
    h5: {
      fontWeight: 600,
    },
    h6: {
      fontWeight: 600,
    },
    button: {
      textTransform: 'none',
      fontWeight: 600,
    },
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          boxShadow: 'none',
          '&:hover': {
            boxShadow: '0 4px 12px rgba(0, 168, 135, 0.15)',
          },
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          borderRadius: 12,
          boxShadow: '0 4px 20px -2px rgba(148, 163, 184, 0.12), 0 2px 8px -1px rgba(148, 163, 184, 0.08)',
        },
      },
    },
    MuiTextField: {
      defaultProps: {
        size: 'small',
      },
    },
    // Pestañas desplazables para que quepan en pantallas angostas
    MuiTabs: {
      defaultProps: {
        variant: 'scrollable',
        scrollButtons: 'auto',
        allowScrollButtonsMobile: true,
      },
    },
    MuiDialogActions: {
      styleOverrides: {
        root: {
          flexWrap: 'wrap',
          gap: 8,
        },
      },
    },
  },
});

// Ajusta el tamaño de los títulos según el ancho de pantalla (celular, tableta, escritorio)
export default responsiveFontSizes(theme);
