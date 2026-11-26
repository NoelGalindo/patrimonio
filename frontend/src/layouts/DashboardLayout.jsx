import { useState } from "react";
import { Outlet, useNavigate, useLocation } from "react-router-dom";
import logo from "../assets/logo_xochi.png";
import useAuth from "../auth/useAuth";
import { ROLES, ROL_INFO } from "../auth/roles";
import {
    Box,
    Drawer,
    AppBar,
    Toolbar,
    List,
    Typography,
    Divider,
    IconButton,
    ListItem,
    ListItemButton,
    ListItemIcon,
    ListItemText,
    Avatar,
    Menu,
    MenuItem,
    useMediaQuery,
    useTheme
} from "@mui/material";

// Icons
import MenuIcon from "@mui/icons-material/Menu";
import DashboardIcon from "@mui/icons-material/DashboardOutlined";
import InventoryIcon from "@mui/icons-material/Inventory2Outlined";
import AddCircleIcon from "@mui/icons-material/AddCircle";
import RemoveCircleIcon from "@mui/icons-material/RemoveCircle";
import SwapHorizIcon from "@mui/icons-material/SwapHorizOutlined";
import SettingsIcon from "@mui/icons-material/SettingsOutlined";
import LogoutIcon from "@mui/icons-material/LogoutOutlined";
import KeyboardArrowDownIcon from "@mui/icons-material/KeyboardArrowDown";
import AccountTreeIcon from "@mui/icons-material/AccountTreeOutlined";
import PeopleIcon from "@mui/icons-material/PeopleOutlined";

const drawerWidth = 280;

export default function DashboardLayout() {
    const theme = useTheme();
    const isMobile = useMediaQuery(theme.breakpoints.down("md"));
    const navigate = useNavigate();
    const location = useLocation();

    const { usuario, logout } = useAuth();

    // State
    const [mobileOpen, setMobileOpen] = useState(false);
    const [anchorElProfile, setAnchorElProfile] = useState(null);

    const handleDrawerToggle = () => {
        setMobileOpen(!mobileOpen);
    };

    const handleProfileMenuOpen = (event) => {
        setAnchorElProfile(event.currentTarget);
    };

    const handleProfileMenuClose = () => {
        setAnchorElProfile(null);
    };

    const handleLogout = () => {
        logout();
        navigate("/login", { replace: true });
    };

    const currentRoleConfig = ROL_INFO[usuario.rol];
    const nombre = usuario.nombreCompleto;
    const iniciales = nombre
        .split(" ")
        .slice(0, 2)
        .map((p) => p[0])
        .join("")
        .toUpperCase();
    const areaTexto = usuario.area?.nombre ?? (usuario.rol === ROLES.USUARIO ? "Sin área asignada" : "Todas las áreas");

    // Menú lateral filtrado por rol (la autorización real la aplica el backend)
    const { ADMINISTRADOR, PATRIMONIO, USUARIO } = ROLES;
    const menuItems = [
        { text: "Inicio", path: "/dashboard", icon: <DashboardIcon /> },
        { text: "Inventario", path: "/dashboard/inventario", icon: <InventoryIcon /> },
        { text: "Altas / Registro", path: "/dashboard/altas", icon: <AddCircleIcon />, roles: [PATRIMONIO] },
        { text: "Bajas", path: "/dashboard/bajas", icon: <RemoveCircleIcon />, roles: [PATRIMONIO, ADMINISTRADOR] },
        { text: "Traspasos", path: "/dashboard/transferencias", icon: <SwapHorizIcon />, roles: [USUARIO, PATRIMONIO, ADMINISTRADOR] },
        { text: "Usuarios", path: "/dashboard/usuarios", icon: <PeopleIcon />, roles: [ADMINISTRADOR, PATRIMONIO] },
        // Patrimonio solo administra áreas; el administrador ve toda la configuración
        usuario.rol === PATRIMONIO
            ? { text: "Áreas", path: "/dashboard/configuracion", icon: <AccountTreeIcon /> }
            : { text: "Configuración", path: "/dashboard/configuracion", icon: <SettingsIcon />, roles: [ADMINISTRADOR] },
    ].filter((item) => !item.roles || item.roles.includes(usuario.rol));

    // Determine Page Title
    const getPageTitle = () => {
        const item = menuItems.find(i => i.path === location.pathname);
        return item ? item.text : "Patrimonio";
    };

    const drawerContent = (
        <Box sx={{ display: "flex", flexDirection: "column", height: "100%", bgcolor: "background.paper" }}>
            {/* Header Brand */}
            <Box
                sx={{
                    p: 3,
                    display: "flex",
                    flexDirection: "column",
                    alignItems: "center",
                    gap: 1.5,
                    borderBottom: "1px solid",
                    borderColor: "divider",
                    bgcolor: "rgba(0, 168, 135, 0.02)"
                }}
            >
                <Box
                    component="img"
                    src={logo}
                    alt="Logo Xochitepec"
                    sx={{ width: 140, height: "auto", objectFit: "contain" }}
                />
                <Box sx={{ textAlign: "center" }}>
                    <Typography variant="subtitle2" sx={{ fontWeight: "800", letterSpacing: 0.5, color: "text.primary" }}>
                        SISTEMA DE GESTIÓN
                    </Typography>
                    <Typography variant="caption" sx={{ color: "primary.main", fontWeight: "700", display: "block" }}>
                        Patrimonio Municipal
                    </Typography>
                </Box>
            </Box>

            {/* Navigation Menu */}
            <Box sx={{ flexGrow: 1, px: 2, py: 3, overflowY: "auto" }}>
                <List sx={{ display: "flex", flexDirection: "column", gap: 0.8, p: 0 }}>
                    {menuItems.map((item) => {
                        const isActive = location.pathname === item.path;
                        return (
                            <ListItem key={item.text} disablePadding>
                                <ListItemButton
                                    onClick={() => {
                                        navigate(item.path);
                                        if (isMobile) setMobileOpen(false);
                                    }}
                                    sx={{
                                        borderRadius: 3.5,
                                        py: 1.3,
                                        px: 2.5,
                                        transition: "all 0.2s ease",
                                        bgcolor: isActive ? "rgba(0, 168, 135, 0.08)" : "transparent",
                                        borderLeft: "4px solid",
                                        borderColor: isActive ? "primary.main" : "transparent",
                                        "&:hover": {
                                            bgcolor: isActive ? "rgba(0, 168, 135, 0.12)" : "rgba(0, 168, 135, 0.04)",
                                            transform: "translateX(4px)"
                                        }
                                    }}
                                >
                                    <ListItemIcon
                                        sx={{
                                            minWidth: 40,
                                            color: isActive ? "primary.main" : "text.secondary"
                                        }}
                                    >
                                        {item.icon}
                                    </ListItemIcon>
                                    <ListItemText
                                        primary={item.text}
                                        slotProps={{
                                            primary: {
                                                fontSize: "0.92rem",
                                                fontWeight: isActive ? "700" : "500",
                                                color: isActive ? "primary.main" : "text.primary"
                                            }
                                        }}
                                    />
                                </ListItemButton>
                            </ListItem>
                        );
                    })}
                </List>
            </Box>

        </Box>
    );

    return (
        <Box sx={{ display: "flex", minHeight: "100vh", bgcolor: "background.default" }}>
            {/* AppBar Top */}
            <AppBar
                position="fixed"
                sx={{
                    width: { md: `calc(100% - ${drawerWidth}px)` },
                    ml: { md: `${drawerWidth}px` },
                    bgcolor: "background.paper",
                    color: "text.primary",
                    boxShadow: "0 1px 3px rgba(0,0,0,0.05)",
                    borderBottom: "1px solid",
                    borderColor: "divider"
                }}
            >
                <Toolbar sx={{ justifyContent: "space-between", px: { xs: 2, md: 3 } }}>
                    <Box sx={{ display: "flex", alignItems: "center", minWidth: 0 }}>
                        <IconButton
                            color="inherit"
                            aria-label="open drawer"
                            edge="start"
                            onClick={handleDrawerToggle}
                            sx={{ mr: { xs: 1, sm: 2 }, display: { md: "none" } }}
                        >
                            <MenuIcon />
                        </IconButton>
                        <Typography variant="h6" noWrap sx={{ fontWeight: "800", color: "text.primary" }}>
                            {getPageTitle()}
                        </Typography>
                    </Box>

                    {/* Menú de usuario */}
                    <Box sx={{ display: "flex", alignItems: "center", gap: 1.5, flexShrink: 0 }}>
                        {/* Menú de Perfil */}
                        <Box
                            onClick={handleProfileMenuOpen}
                            sx={{
                                display: "flex",
                                alignItems: "center",
                                gap: 1,
                                cursor: "pointer",
                                p: 0.5,
                                px: 1.2,
                                borderRadius: 3,
                                transition: "background 0.2s",
                                "&:hover": {
                                    bgcolor: "rgba(0,0,0,0.03)"
                                }
                            }}
                        >
                            <Avatar
                                sx={{
                                    bgcolor: "primary.main",
                                    color: "white",
                                    fontWeight: "bold",
                                    width: 34,
                                    height: 34,
                                    fontSize: "0.85rem"
                                }}
                            >
                                {iniciales}
                            </Avatar>
                            <Box sx={{ display: { xs: "none", lg: "block" } }}>
                                <Typography variant="body2" sx={{ fontWeight: "700", lineHeight: 1.2 }}>
                                    {nombre}
                                </Typography>
                                <Typography variant="caption" sx={{ color: "text.secondary", fontSize: "0.7rem", display: "block" }}>
                                    {currentRoleConfig.label}
                                </Typography>
                            </Box>
                            <KeyboardArrowDownIcon sx={{ color: "text.secondary", fontSize: 18 }} />
                        </Box>
                        <Menu
                            anchorEl={anchorElProfile}
                            open={Boolean(anchorElProfile)}
                            onClose={handleProfileMenuClose}
                            slotProps={{
                                paper: { sx: { borderRadius: 3, width: 260, maxWidth: "calc(100vw - 32px)", mt: 1, p: 0.5 } }
                            }}
                        >
                            <Box sx={{ px: 2, py: 1 }}>
                                <Typography variant="body2" sx={{ fontWeight: 700 }}>{nombre}</Typography>
                                <Typography variant="caption" color="text.secondary" sx={{ display: "block" }}>{usuario.correo}</Typography>
                                <Typography variant="caption" color="text.secondary" sx={{ display: "block" }}>{areaTexto}</Typography>
                                <Typography variant="caption" sx={{ display: "block", color: `${currentRoleConfig.color}.main`, fontWeight: 700 }}>{currentRoleConfig.label}</Typography>
                            </Box>
                            <Divider sx={{ my: 0.5 }} />
                            <MenuItem onClick={handleLogout} sx={{ borderRadius: 2, color: "error.main" }}>
                                <ListItemIcon sx={{ color: "error.main" }}><LogoutIcon fontSize="small" /></ListItemIcon>
                                <ListItemText primary="Cerrar Sesión" slotProps={{ primary: { fontSize: "0.85rem", fontWeight: "600" } }} />
                            </MenuItem>
                        </Menu>
                    </Box>
                </Toolbar>
            </AppBar>

            {/* Navigation Drawer responsive */}
            <Box
                component="nav"
                sx={{ width: { md: drawerWidth }, flexShrink: { md: 0 } }}
                aria-label="navigation panels"
            >
                {/* Mobile Temporary Drawer */}
                <Drawer
                    variant="temporary"
                    open={mobileOpen}
                    onClose={handleDrawerToggle}
                    ModalProps={{
                        keepMounted: true // Better open performance on mobile.
                    }}
                    sx={{
                        display: { xs: "block", md: "none" },
                        "& .MuiDrawer-paper": { boxSizing: "border-box", width: drawerWidth, maxWidth: "85vw", borderRight: "none" }
                    }}
                >
                    {drawerContent}
                </Drawer>

                {/* Desktop Permanent Drawer */}
                <Drawer
                    variant="permanent"
                    sx={{
                        display: { xs: "none", md: "block" },
                        "& .MuiDrawer-paper": { boxSizing: "border-box", width: drawerWidth, borderRight: "1px solid", borderColor: "divider" }
                    }}
                    open
                >
                    {drawerContent}
                </Drawer>
            </Box>

            {/* Main Content Pane */}
            <Box
                component="main"
                sx={{
                    flexGrow: 1,
                    width: { xs: "100%", md: `calc(100% - ${drawerWidth}px)` },
                    minWidth: 0
                }}
            >
                {/* Espaciador con la altura real de la barra superior (56 px en celular, 64 px en escritorio) */}
                <Toolbar />
                <Box
                    sx={{
                        p: { xs: 2, sm: 3, md: 4 },
                        display: "flex",
                        flexDirection: "column",
                        gap: 3
                    }}
                >
                    <Outlet />
                </Box>
            </Box>
        </Box>
    );
}